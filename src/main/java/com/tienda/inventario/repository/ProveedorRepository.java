package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findFirstByNombre(String nombre);
}
