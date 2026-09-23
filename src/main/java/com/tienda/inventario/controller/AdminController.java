package com.tienda.inventario.controller;

import com.tienda.inventario.dto.ResetRequest;
import com.tienda.inventario.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Borra TODOS los productos, ventas, compras y su historial de inventario.
     * Requiere mandar {"confirmacion": "BORRAR"} en el body para evitar un
     * reset accidental. No toca usuarios, roles, categorias, proveedores ni
     * tasas de cambio.
     */
    @PostMapping("/reset-productos")
    public ResponseEntity<String> resetProductos(@Valid @RequestBody ResetRequest request) {
        if (!"BORRAR".equals(request.getConfirmacion())) {
            throw new IllegalArgumentException(
                    "Para confirmar el reset, envia en el body: \"confirmacion\": \"BORRAR\"");
        }
        adminService.resetProductosYVentas();
        return ResponseEntity.ok("Productos, ventas, compras e historial de inventario borrados");
    }

    /**
     * Deja todo el negocio desde cero, conservando SOLO los usuarios (y sus
     * roles y la configuracion del sistema). Irreversible: requiere mandar
     * {"confirmacion": "BORRAR TODO"} en el body.
     */
    @PostMapping("/reset-todo")
    public ResponseEntity<String> resetTodo(@Valid @RequestBody ResetRequest request) {
        if (!"BORRAR TODO".equals(request.getConfirmacion())) {
            throw new IllegalArgumentException(
                    "Para confirmar el reset, envia en el body: \"confirmacion\": \"BORRAR TODO\"");
        }
        adminService.resetTodoMenosUsuarios();
        return ResponseEntity.ok("Todos los datos borrados. Los usuarios se conservaron");
    }
}
