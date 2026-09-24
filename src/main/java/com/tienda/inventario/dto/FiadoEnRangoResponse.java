package com.tienda.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Fiados de TODOS los deudores en un rango de fechas (a diferencia de
// MovimientoDeudaResponse, que es el historial de un solo deudor y por eso
// no necesita su nombre). Se usa para mostrar el fiado por separado del
// efectivo en Estadisticas, Historial de ventas e Inicio.
//
// "pagado" es cuanto de este fiado ya cubrieron los abonos del deudor (los
// abonos se aplican primero a los fiados mas antiguos) y "estado" es
// PAGADO, PARCIAL o PENDIENTE segun eso.
public record FiadoEnRangoResponse(
        Long id,
        String deudorNombre,
        BigDecimal monto,
        String descripcion,
        LocalDateTime fecha,
        List<DetalleFiadoResponse> detalles,
        BigDecimal pagado,
        String estado
) {
}
