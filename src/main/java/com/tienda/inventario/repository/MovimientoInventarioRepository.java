package com.tienda.inventario.repository;

import com.tienda.inventario.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByProductoIdOrderByFechaDesc(Long productoId);
    List<MovimientoInventario> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);
    List<MovimientoInventario> findByFechaGreaterThanEqual(LocalDateTime desde);
}
