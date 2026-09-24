package com.tienda.inventario.dto;

import java.math.BigDecimal;

// Resumen de fiados para la pantalla de Deudores y la tarjeta de Inicio.
// Todos los totales cuentan a todos los deudores (activos y desactivados), asi
// totalFiado - totalCobrado = porCobrar (el mismo numero de "Deuda pendiente"
// de Fiscalizacion). fiadoEnRango/cobradoEnRango es lo mismo, pero de un rango
// de fechas (ej: hoy).
public record ResumenDeudasResponse(
        BigDecimal totalFiado,
        BigDecimal totalCobrado,
        BigDecimal porCobrar,
        int deudoresConDeuda,
        BigDecimal fiadoEnRango,
        BigDecimal cobradoEnRango
) {
}
