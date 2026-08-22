package com.tienda.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemVentaRequest {

    @NotBlank
    private String codigoBarras;

    // Para productos normales. Para productos "vendidos por peso" se usa
    // "peso" en su lugar (ver Producto.vendidoPorPeso) y este campo se ignora.
    @Min(1)
    private Integer cantidad;

    // Solo para productos vendidos por peso: cuanto peso (ej: libras) se vendio.
    private BigDecimal peso;
}
