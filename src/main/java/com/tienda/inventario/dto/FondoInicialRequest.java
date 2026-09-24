package com.tienda.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

// Efectivo con el que se empieza a llevar la cuenta de la caja, y desde que dia.
public record FondoInicialRequest(
        @NotNull(message = "no debe estar vacio")
        @DecimalMin(value = "0", message = "no puede ser negativo")
        BigDecimal monto,

        @NotNull(message = "no debe estar vacia")
        LocalDate fecha
) {
}
