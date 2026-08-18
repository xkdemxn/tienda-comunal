package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Rol;
import com.tienda.inventario.enums.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(RolNombre nombre);
}
