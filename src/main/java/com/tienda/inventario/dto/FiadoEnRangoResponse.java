package com.tienda.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Fiados de TODOS los deudores en un rango de fechas (a diferencia de
// MovimientoDeudaResponse, que es el historial de un solo deudor y por eso
// no necesita su nombre). Se usa para mostrar el fiado por separado del
// efectivo en Estadisticas, Historial de ventas e Inicio.
public record FiadoEnRangoResponse(
        Long id,
        String deudorNombre,
        BigDecimal monto,
        String descripcion,
        LocalDateTime fecha,
        List<DetalleFiadoResponse> detalles
) {
}
