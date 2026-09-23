package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroRequest {

    @NotBlank(message = "no debe estar vacio")
    private String nombre;

    // DTO de peticion, no toca la base de datos: el campo se llama "usuario"
    // para que los mensajes de validacion digan "usuario" y no "email"
    // (la entidad Usuario/columna de la BD se sigue llamando "email").
    @NotBlank(message = "no debe estar vacio")
    private String usuario;

    @NotBlank(message = "no debe estar vacia")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;

    // "ADMIN" o "VENDEDOR". Si viene vacio, se asigna VENDEDOR por defecto.
    // OJO: en POST /api/auth/registro (publico) este campo se ignora, siempre
    // se crea VENDEDOR. Solo se respeta en POST /api/usuarios (admin).
    private String rol;
}
