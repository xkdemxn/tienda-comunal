package com.tienda.inventario.controller;

import com.tienda.inventario.dto.DeudorRequest;
import com.tienda.inventario.dto.DeudorResponse;
import com.tienda.inventario.dto.FiadoRequest;
import com.tienda.inventario.dto.MovimientoDeudaRequest;
import com.tienda.inventario.dto.MovimientoDeudaResponse;
import com.tienda.inventario.entity.Deudor;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.security.UsuarioPrincipal;
import com.tienda.inventario.service.DeudorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deudores")
@RequiredArgsConstructor
public class DeudorController {

    private final DeudorService deudorService;

    @GetMapping
    public ResponseEntity<List<DeudorResponse>> listar() {
        return ResponseEntity.ok(deudorService.listar());
    }

    @PostMapping
    public ResponseEntity<Deudor> crear(@Valid @RequestBody DeudorRequest request) {
        return ResponseEntity.ok(deudorService.crear(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        deudorService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<MovimientoDeudaResponse>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(deudorService.historial(id));
    }

    @PostMapping("/{id}/fiado")
    public ResponseEntity<MovimientoDeudaResponse> fiado(@PathVariable Long id,
                                                           @Valid @RequestBody MovimientoDeudaRequest request,
                                                           @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        return ResponseEntity.ok(deudorService.registrarFiado(id, request, usuario));
    }

    // Fiado eligiendo productos del stock (como una venta, pero sin cobrar).
    @PostMapping("/{id}/fiado-productos")
    public ResponseEntity<MovimientoDeudaResponse> fiadoConProductos(@PathVariable Long id,
                                                                       @Valid @RequestBody FiadoRequest request,
                                                                       @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        return ResponseEntity.ok(deudorService.registrarFiadoConProductos(id, request, usuario));
    }

    @PostMapping("/{id}/abono")
    public ResponseEntity<MovimientoDeudaResponse> abono(@PathVariable Long id,
                                                           @Valid @RequestBody MovimientoDeudaRequest request,
                                                           @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        return ResponseEntity.ok(deudorService.registrarAbono(id, request, usuario));
    }
}
