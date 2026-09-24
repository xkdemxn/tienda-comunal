package com.tienda.inventario.repository;

import com.tienda.inventario.entity.MovimientoDeuda;
import com.tienda.inventario.enums.TipoMovimientoDeuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoDeudaRepository extends JpaRepository<MovimientoDeuda, Long> {
    List<MovimientoDeuda> findByDeudorIdOrderByFechaDesc(Long deudorId);
    List<MovimientoDeuda> findByDeudorId(Long deudorId);
    List<MovimientoDeuda> findByTipoAndFechaBetweenOrderByFechaDesc(
            TipoMovimientoDeuda tipo, LocalDateTime desde, LocalDateTime hasta);

    // Total historico de un tipo de movimiento, de todos los deudores (tambien
    // los desactivados: desactivar no borra ni "paga" ninguna deuda).
    @Query("select coalesce(sum(m.monto), 0) from MovimientoDeuda m where m.tipo = :tipo")
    BigDecimal sumaPorTipo(@Param("tipo") TipoMovimientoDeuda tipo);

    // Total de un tipo de movimiento en un rango de fechas, de todos los deudores.
    @Query("select coalesce(sum(m.monto), 0) from MovimientoDeuda m "
            + "where m.tipo = :tipo and m.fecha between :desde and :hasta")
    BigDecimal sumaEnRangoPorTipo(@Param("tipo") TipoMovimientoDeuda tipo,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta);
}
