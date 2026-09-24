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

    // Fondo inicial de caja: el efectivo con el que se empieza a llevar la
    // cuenta y desde que dia. Se confirma una sola vez; de ahi en adelante el
    // arqueo calcula el efectivo esperado de cualquier periodo. Nullable a
    // proposito (la tabla ya tiene su fila; una columna NOT NULL nueva fallaria).
    @Column(precision = 12, scale = 2)
    private java.math.BigDecimal fondoInicialMonto;

    private java.time.LocalDate fondoInicialFecha;

    private LocalDateTime actualizadoEn;
}
