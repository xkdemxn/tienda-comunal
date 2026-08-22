package com.tienda.inventario.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        Boolean activo,
        List<String> roles,
        LocalDateTime creadoEn
) {
}
