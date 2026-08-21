package com.tienda.inventario.repository;

import com.tienda.inventario.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    boolean existsByCodigoBarras(String codigoBarras);
    Optional<Producto> findByCodigoBarrasCaja(String codigoBarrasCaja);
    boolean existsByCodigoBarrasCaja(String codigoBarrasCaja);
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    // JOIN FETCH explicito: sin esto, categoria/proveedor quedan como proxy lazy
    // sin inicializar, y Hibernate6Module los serializa como null aunque el
    // producto SI tenga categoria asignada (por diseño, para evitar N+1 al
    // serializar) — eso es lo que rompia el filtro por categoria en el frontend.
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor")
    @Override
    List<Producto> findAll();

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria WHERE p.activo = true")
    List<Producto> findByActivoTrue();

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();
}
