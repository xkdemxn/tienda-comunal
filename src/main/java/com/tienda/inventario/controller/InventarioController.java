package com.tienda.inventario.controller;

import com.tienda.inventario.dto.AjusteInventarioRequest;
import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.enums.TipoMovimiento;
import com.tienda.inventario.security.UsuarioPrincipal;
import com.tienda.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    /**
     * Ajuste manual de stock: mermas, productos danados, correcciones de conteo fisico, etc.
     * Todo ajuste queda registrado en el kardex con el motivo indicado.
     */
    @PostMapping("/ajuste")
    public ResponseEntity<Producto> ajustar(@Valid @RequestBody AjusteInventarioRequest request,
                                             @AuthenticationPrincipal UsuarioPrincipal principal) {
        Usuario usuario = principal.getUsuario();
        Producto producto;

        if (Boolean.TRUE.equals(request.getEsPositivo())) {
            producto = inventarioService.aumentarStock(request.getCodigoBarras(), request.getCantidad(),
                    TipoMovimiento.AJUSTE_POSITIVO, usuario, request.getMotivo());
        } else {
            producto = inventarioService.reducirStock(request.getCodigoBarras(), request.getCantidad(),
                    TipoMovimiento.AJUSTE_NEGATIVO, usuario, request.getMotivo());
        }

        return ResponseEntity.ok(producto);
    }
}
