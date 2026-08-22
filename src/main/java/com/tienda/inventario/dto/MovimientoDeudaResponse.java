package com.tienda.inventario.dto;

import com.tienda.inventario.enums.TipoMovimientoDeuda;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoDeudaResponse(
        Long id,
        TipoMovimientoDeuda tipo,
        BigDecimal monto,
        String descripcion,
        LocalDateTime fecha
) {
}
