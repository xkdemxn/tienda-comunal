package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarPasswordRequest {

    @NotBlank
    private String passwordActual;

    @NotBlank
    @Size(min = 6, message = "La contrasena nueva debe tener al menos 6 caracteres")
    private String passwordNueva;
}
