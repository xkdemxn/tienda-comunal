package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetRequest {

    // Debe ser exactamente "BORRAR". Es una salvaguarda para que un reset
    // tan destructivo no se dispare por accidente con un click de mas en Postman.
    @NotBlank
    private String confirmacion;
}
