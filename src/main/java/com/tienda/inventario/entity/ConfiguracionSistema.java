package com.tienda.inventario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_sistema")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionSistema {

    // Fila unica de configuracion global.
    public static final Long ID_UNICO = 1L;

    @Id
    private Long id = ID_UNICO;

    @Column(nullable = false)
    private boolean suscripcionActiva = true;

    @Column(length = 255)
    private String mensaje = "Servicio suspendido. Contacta a soporte para reactivar tu cuenta.";

    private LocalDateTime actualizadoEn;
}
