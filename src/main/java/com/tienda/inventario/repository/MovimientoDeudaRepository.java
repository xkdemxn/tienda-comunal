package com.tienda.inventario.repository;

import com.tienda.inventario.entity.MovimientoDeuda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoDeudaRepository extends JpaRepository<MovimientoDeuda, Long> {
    List<MovimientoDeuda> findByDeudorIdOrderByFechaDesc(Long deudorId);
    List<MovimientoDeuda> findByDeudorId(Long deudorId);
}
