package com.tienda.inventario.repository;

import com.tienda.inventario.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Detalles de ventas de productos "por peso" (ej: carne) en un rango de
    // fechas. Estos NUNCA pasan por el kardex (MovimientoInventario), asi
    // que hay que sacar su ganancia real directo de las ventas.
    @Query("SELECT d FROM DetalleVenta d JOIN FETCH d.producto WHERE d.peso IS NOT NULL " +
            "AND d.venta.fecha BETWEEN :desde AND :hasta")
    List<DetalleVenta> findConPesoEnRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
