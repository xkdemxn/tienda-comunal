package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EscaneoRequest {

    @NotBlank
    private String codigoBarras;
}
