package com.tienda.inventario.controller;

import com.tienda.inventario.entity.Proveedor;
import com.tienda.inventario.repository.ProveedorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    @GetMapping
    public ResponseEntity<List<Proveedor>> listar() {
        return ResponseEntity.ok(proveedorRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody Proveedor proveedor) {
        return ResponseEntity.ok(proveedorRepository.save(proveedor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
