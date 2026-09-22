package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    // DTO de peticion, no toca la base de datos: el campo se llama "usuario"
    // para que los mensajes de validacion digan "usuario" y no "email"
    // (la entidad Usuario/columna de la BD se sigue llamando "email").
    @NotBlank
    private String usuario;

    @NotBlank
    private String password;
}
