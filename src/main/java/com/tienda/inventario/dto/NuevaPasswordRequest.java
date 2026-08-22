package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NuevaPasswordRequest(
        @NotBlank
        @Size(min = 6, message = "La contrasena nueva debe tener al menos 6 caracteres")
        String passwordNueva
) {
}
