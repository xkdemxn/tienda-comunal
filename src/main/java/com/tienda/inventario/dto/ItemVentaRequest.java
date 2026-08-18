package com.tienda.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemVentaRequest {

    @NotBlank
    private String codigoBarras;

    @Min(1)
    private Integer cantidad;
}
