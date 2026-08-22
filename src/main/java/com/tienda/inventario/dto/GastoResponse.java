package com.tienda.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GastoResponse(
        Long id,
        String descripcion,
        BigDecimal monto,
        LocalDateTime fecha
) {
}
