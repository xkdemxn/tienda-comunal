package com.tienda.inventario.controller;

import com.tienda.inventario.dto.AuthResponse;
import com.tienda.inventario.dto.CambiarPasswordRequest;
import com.tienda.inventario.dto.LoginRequest;
import com.tienda.inventario.dto.RegistroRequest;
import com.tienda.inventario.security.UsuarioPrincipal;
import com.tienda.inventario.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Requiere estar logueado (cualquier rol): cambia tu propia contrasena.
    // No cae bajo el permitAll de /api/auth/** (ver SecurityConfig).
    @PutMapping("/password")
    public ResponseEntity<Void> cambiarPassword(@AuthenticationPrincipal UsuarioPrincipal principal,
                                                 @Valid @RequestBody CambiarPasswordRequest request) {
        authService.cambiarPassword(principal.getId(), request.getPasswordActual(), request.getPasswordNueva());
        return ResponseEntity.noContent().build();
    }
}
