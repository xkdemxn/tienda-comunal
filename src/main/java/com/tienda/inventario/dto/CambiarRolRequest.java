package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarRolRequest(
        @NotBlank(message = "no debe estar vacio (ADMIN o VENDEDOR)") String rol
) {
}
