package com.tienda.inventario.service;

import com.tienda.inventario.dto.AuthResponse;
import com.tienda.inventario.dto.LoginRequest;
import com.tienda.inventario.dto.RegistroRequest;
import com.tienda.inventario.entity.Rol;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.enums.RolNombre;
import com.tienda.inventario.repository.RolRepository;
import com.tienda.inventario.repository.UsuarioRepository;
import com.tienda.inventario.security.JwtUtil;
import com.tienda.inventario.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Publico (sin login): siempre crea VENDEDOR, sin importar que "rol" mande
    // quien llama. Crear un ADMIN requiere estar logueado como ADMIN (ver
    // crearUsuarioComoAdmin) para que nadie se pueda auto-asignar el rol.
    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        Usuario usuario = crearUsuarioInterno(request, RolNombre.VENDEDOR);

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        String token = jwtUtil.generarToken(principal);

        return new AuthResponse(token, usuario.getUsuario(), usuario.getNombre(),
                List.of("ROLE_VENDEDOR"));
    }

    // Solo para uso desde un endpoint ya protegido con hasRole("ADMIN"). Aqui
    // si se respeta el rol pedido (ADMIN o VENDEDOR).
    @Transactional
    public Usuario crearUsuarioComoAdmin(RegistroRequest request) {
        RolNombre rolSolicitado;
        try {
            rolSolicitado = request.getRol() == null || request.getRol().isBlank()
                    ? RolNombre.VENDEDOR
                    : RolNombre.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol invalido. Usa ADMIN o VENDEDOR");
        }
        return crearUsuarioInterno(request, rolSolicitado);
    }

    private Usuario crearUsuarioInterno(RegistroRequest request, RolNombre rolNombre) {
        if (usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre de acceso");
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "El rol " + rolNombre + " no existe en la base de datos. Ejecuta el seeder de roles."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setUsuario(request.getUsuario());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRoles(roles);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    // Cambio de contrasena propia: exige la contrasena actual.
    @Transactional
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contrasena actual no es correcta");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    // Reseteo desde un endpoint protegido con hasRole("ADMIN"): no exige la
    // contrasena anterior, para cuando otro usuario se olvido la suya.
    @Transactional
    public void resetearPassword(Long usuarioId, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    // Cambia el rol de un usuario (queda con ese unico rol). Nadie puede
    // cambiar su propio rol: si el unico ADMIN se pasara a VENDEDOR, el
    // sistema quedaria sin administrador.
    @Transactional
    public Usuario cambiarRol(Long usuarioId, String rolNuevo, Long solicitanteId) {
        if (usuarioId.equals(solicitanteId)) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol");
        }

        RolNombre rolNombre;
        try {
            rolNombre = RolNombre.valueOf(rolNuevo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol invalido. Usa ADMIN o VENDEDOR");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "El rol " + rolNombre + " no existe en la base de datos."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);
        usuario.setRoles(roles);
        return usuarioRepository.save(usuario);
    }

    // Borra al usuario de verdad. Si ya tiene ventas/compras/etc. registradas
    // la base lo impide (esos registros apuntan a el): en ese caso hay que
    // desactivarlo en vez de borrarlo, para no perder el historial.
    @Transactional
    public void eliminarUsuario(Long usuarioId, Long solicitanteId) {
        if (usuarioId.equals(solicitanteId)) {
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        try {
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Este usuario ya tiene ventas u otros movimientos registrados y no se puede eliminar. "
                            + "Desactivalo con PUT /api/usuarios/" + usuarioId + "/activo");
        }
    }

    // Activa o desactiva un usuario (desactivado no puede entrar, pero se
    // conserva su historial).
    @Transactional
    public Usuario cambiarActivo(Long usuarioId, boolean activo, Long solicitanteId) {
        if (usuarioId.equals(solicitanteId)) {
            throw new IllegalArgumentException("No puedes desactivar tu propio usuario");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        String token = jwtUtil.generarToken(principal);

        List<String> roles = principal.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        return new AuthResponse(token, usuario.getUsuario(), usuario.getNombre(), roles);
    }
}
