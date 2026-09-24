package com.tienda.inventario.dto;

import java.math.BigDecimal;

public record DeudorResponse(
        Long id,
        String nombre,
        String telefono,
        BigDecimal saldoActual,
        Boolean activo
) {
}
