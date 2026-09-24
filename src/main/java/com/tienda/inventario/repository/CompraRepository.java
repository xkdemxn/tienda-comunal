package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Compra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    // Trae proveedor, detalles y productos en la misma consulta: son relaciones
    // "lazy", y Hibernate6Module serializa las no cargadas como null, con lo que
    // el historial de compras salia sin proveedor ni productos.
    @EntityGraph(attributePaths = {"proveedor", "detalles", "detalles.producto"})
    List<Compra> findAllByOrderByFechaDesc();
}
