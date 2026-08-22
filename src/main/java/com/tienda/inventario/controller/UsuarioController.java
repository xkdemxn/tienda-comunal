package com.tienda.inventario.controller;

import com.tienda.inventario.dto.NuevaPasswordRequest;
import com.tienda.inventario.dto.RegistroRequest;
import com.tienda.inventario.dto.UsuarioResponse;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.repository.UsuarioRepository;
import com.tienda.inventario.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<UsuarioResponse> usuarios = usuarioRepository.findAll().stream()
                .map(this::mapear)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    // Solo llega hasta aca si esta logueado como ADMIN (ver SecurityConfig).
    // A diferencia de /api/auth/registro, aca si se respeta el rol pedido.
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.crearUsuarioComoAdmin(request);
        return ResponseEntity.ok(mapear(usuario));
    }

    // Resetea la contrasena de otro usuario sin pedir la anterior (ej: se le
    // olvido). Solo llega hasta aca si esta logueado como ADMIN.
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> resetearPassword(@PathVariable Long id,
                                                  @Valid @RequestBody NuevaPasswordRequest request) {
        authService.resetearPassword(id, request.passwordNueva());
        return ResponseEntity.noContent().build();
    }

    private UsuarioResponse mapear(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getActivo(),
                u.getRoles().stream().map(r -> r.getNombre().name()).toList(),
                u.getCreadoEn()
        );
    }
}
