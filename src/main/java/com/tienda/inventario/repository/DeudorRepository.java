package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Deudor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeudorRepository extends JpaRepository<Deudor, Long> {
    List<Deudor> findByActivoTrueOrderByNombreAsc();
    List<Deudor> findAllByOrderByNombreAsc();
}
