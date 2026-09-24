package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Compra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    // Trae proveedor, detalles y productos en la misma consulta: son relaciones
    // "lazy", y Hibernate6Module serializa las no cargadas como null, con lo que
    // el historial de compras salia sin proveedor ni productos.
    @EntityGraph(attributePaths = {"proveedor", "detalles", "detalles.producto"})
    List<Compra> findAllByOrderByFechaDesc();

    @Query("select coalesce(sum(c.total), 0) from Compra c where c.fecha between :desde and :hasta")
    BigDecimal sumaTotalEnRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    long countByFechaBetween(LocalDateTime desde, LocalDateTime hasta);
}
