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
    List<Producto> findByActivoTrue();

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();
}
