package com.tienda.inventario.controller;

import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> stockBajo() {
        return ResponseEntity.ok(productoService.productosConStockBajo());
    }

    // Consulta si un codigo escaneado corresponde al codigo de CAJA de algun
    // producto (para diferenciarlo del escaneo de la unidad)
    @GetMapping("/caja/{codigo}")
    public ResponseEntity<Producto> obtenerPorCodigoCaja(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.obtenerPorCodigoCaja(codigo));
    }

    @GetMapping(value = "/{id}/codigo-barras", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> codigoBarras(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.generarImagenCodigoBarras(id));
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.crear(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
