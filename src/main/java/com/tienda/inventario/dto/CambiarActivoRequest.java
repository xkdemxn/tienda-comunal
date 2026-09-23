package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarActivoRequest(
        @NotNull(message = "no debe estar vacio (true o false)") Boolean activo
) {
}
