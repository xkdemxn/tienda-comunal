package com.tienda.inventario.dto;

import java.math.BigDecimal;

public record DetalleFiadoResponse(
        String producto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
