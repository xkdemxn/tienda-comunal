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

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        RolNombre rolSolicitado;
        try {
            rolSolicitado = request.getRol() == null || request.getRol().isBlank()
                    ? RolNombre.VENDEDOR
                    : RolNombre.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol invalido. Usa ADMIN o VENDEDOR");
        }

        Rol rol = rolRepository.findByNombre(rolSolicitado)
                .orElseThrow(() -> new IllegalStateException(
                        "El rol " + rolSolicitado + " no existe en la base de datos. Ejecuta el seeder de roles."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRoles(roles);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        String token = jwtUtil.generarToken(principal);

        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre(),
                List.of("ROLE_" + rolSolicitado.name()));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        String token = jwtUtil.generarToken(principal);

        List<String> roles = principal.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre(), roles);
    }
}
