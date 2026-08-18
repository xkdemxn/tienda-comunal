package com.tienda.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemCompraRequest {

    @NotBlank
    private String codigoBarras;

    @Min(1)
    private Integer cantidad;

    @NotNull
    private BigDecimal costoUnitario;
}
