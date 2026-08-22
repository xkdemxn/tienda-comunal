package com.tienda.inventario.dto;

import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        Long id,
        String producto,
        String codigoBarras,
        Integer cantidad,
        String motivo,
        LocalDateTime fecha
) {
}
