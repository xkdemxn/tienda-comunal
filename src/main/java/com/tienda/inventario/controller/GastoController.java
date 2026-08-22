package com.tienda.inventario.controller;

import com.tienda.inventario.dto.GastoRequest;
import com.tienda.inventario.dto.GastoResponse;
import com.tienda.inventario.security.UsuarioPrincipal;
import com.tienda.inventario.service.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;

    @GetMapping
    public ResponseEntity<List<GastoResponse>> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(gastoService.listar(desde, hasta));
    }

    @PostMapping
    public ResponseEntity<GastoResponse> registrar(@Valid @RequestBody GastoRequest request,
                                                     @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(gastoService.registrar(request, principal.getUsuario()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        gastoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
