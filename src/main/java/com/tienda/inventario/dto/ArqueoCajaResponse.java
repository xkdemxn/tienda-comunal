package com.tienda.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Efectivo que deberia haber en caja al cierre de un periodo, calculado a
// partir del fondo inicial (que se confirma una sola vez) y de todo el dinero
// que entro y salio desde esa fecha.
//
// estado: SIN_FONDO (todavia no se configuro), ANTES_DEL_FONDO (el periodo
// termina antes de la fecha del fondo) u OK.
public record ArqueoCajaResponse(
        String estado,
        BigDecimal fondoInicial,
        LocalDate fondoFecha,
        // Desde cuando se cuentan los movimientos de este periodo: el inicio
        // pedido, o la fecha del fondo si el periodo empezaba antes.
        LocalDateTime desdeEfectivo,
        // Efectivo esperado justo antes de empezar el periodo (fondo + todo lo
        // que paso entre la fecha del fondo y el inicio del periodo).
        BigDecimal efectivoAlInicio,
        BigDecimal ventasEfectivo,
        BigDecimal abonosCobrados,
        BigDecimal gastos,
        BigDecimal compras,
        BigDecimal efectivoEsperado
) {
}
