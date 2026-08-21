package com.tienda.inventario.controller;

import com.tienda.inventario.dto.VentaRequest;
import com.tienda.inventario.dto.VentaResponse;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.security.UsuarioPrincipal;
import com.tienda.inventario.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<VentaResponse>> listarVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ventaService.listarVentas(desde, hasta));
    }
}
