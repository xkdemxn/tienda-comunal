package com.tienda.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjusteInventarioRequest {

    @NotBlank
    private String codigoBarras;

    @Min(1)
    private Integer cantidad;

    @NotBlank
    private String motivo;

    @NotNull
    private Boolean esPositivo; // true = suma stock, false = resta stock
}
