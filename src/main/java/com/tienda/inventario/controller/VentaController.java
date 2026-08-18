package com.tienda.inventario.controller;

import com.tienda.inventario.dto.VentaRequest;
import com.tienda.inventario.dto.VentaResponse;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.security.UsuarioPrincipal;
import com.tienda.inventario.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<VentaResponse> registrarVenta(@Valid @RequestBody VentaRequest request,
                                                          @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        return ResponseEntity.ok(ventaService.registrarVenta(request, usuario));
    }
}
