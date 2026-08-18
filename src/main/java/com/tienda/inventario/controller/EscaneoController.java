package com.tienda.inventario.controller;

import com.tienda.inventario.dto.EscaneoRequest;
import com.tienda.inventario.dto.ProductoEscaneadoResponse;
import com.tienda.inventario.service.EscaneoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Este endpoint es el que consume el frontend (pagina con la camara del celular)
 * cada vez que se lee un codigo de barras, para mostrar en pantalla que producto es
 * antes de confirmar la venta.
 */
@RestController
@RequestMapping("/api/escaneo")
@RequiredArgsConstructor
public class EscaneoController {

    private final EscaneoService escaneoService;

    @PostMapping
    public ResponseEntity<ProductoEscaneadoResponse> escanear(@Valid @RequestBody EscaneoRequest request) {
        return ResponseEntity.ok(escaneoService.buscarPorCodigo(request.getCodigoBarras()));
    }

    // Tambien disponible por GET para pruebas rapidas desde el navegador
    @GetMapping("/{codigoBarras}")
    public ResponseEntity<ProductoEscaneadoResponse> escanearPorPath(@PathVariable String codigoBarras) {
        return ResponseEntity.ok(escaneoService.buscarPorCodigo(codigoBarras));
    }
}
