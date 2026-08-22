package com.tienda.inventario.dto;

import java.math.BigDecimal;

public record FiscalizacionPorPesoDto(
        String producto,
        BigDecimal pesoVendido,
        BigDecimal ingresoTotal,
        BigDecimal gananciaEstimada // null si el producto no tiene precio de compra configurado
) {
}
