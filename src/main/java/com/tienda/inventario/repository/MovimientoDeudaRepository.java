package com.tienda.inventario.repository;

import com.tienda.inventario.entity.MovimientoDeuda;
import com.tienda.inventario.enums.TipoMovimientoDeuda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoDeudaRepository extends JpaRepository<MovimientoDeuda, Long> {
    List<MovimientoDeuda> findByDeudorIdOrderByFechaDesc(Long deudorId);
    List<MovimientoDeuda> findByDeudorId(Long deudorId);
    List<MovimientoDeuda> findByTipoAndFechaBetweenOrderByFechaDesc(
            TipoMovimientoDeuda tipo, LocalDateTime desde, LocalDateTime hasta);
}
