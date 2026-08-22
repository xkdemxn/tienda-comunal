package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeudorRequest {

    @NotBlank
    private String nombre;

    private String telefono;
}
