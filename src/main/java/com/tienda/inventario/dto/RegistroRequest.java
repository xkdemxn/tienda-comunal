package com.tienda.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroRequest {

    @NotBlank
    private String nombre;

    // Se sigue llamando "email" por dentro (mismo campo/columna de siempre),
    // pero ya no exige formato de correo: es el "usuario de acceso", puede
    // ser cualquier texto (ej. "cajero1").
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;

    // "ADMIN" o "VENDEDOR". Si viene vacio, se asigna VENDEDOR por defecto.
    // OJO: en POST /api/auth/registro (publico) este campo se ignora, siempre
    // se crea VENDEDOR. Solo se respeta en POST /api/usuarios (admin).
    private String rol;
}
