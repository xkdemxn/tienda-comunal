package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    // Se sigue llamando "email" por dentro (mismo campo/columna de siempre,
    // sin tocar la base de datos), pero ya no exige formato de correo: el
    // login es por "usuario de acceso", puede ser cualquier texto.
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
