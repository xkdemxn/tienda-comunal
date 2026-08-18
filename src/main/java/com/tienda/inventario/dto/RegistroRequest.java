package com.tienda.inventario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;

    // "ADMIN" o "VENDEDOR". Si viene vacio, se asigna VENDEDOR por defecto.
    private String rol;
}
