package com.tienda.inventario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Guarda la ultima tasa de cambio obtenida de la API externa,
 * para no consultarla en cada venta (se actualiza via job programado).
 */
@Entity
@Table(name = "tasas_cambio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TasaCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String monedaOrigen;

    @Column(nullable = false, length = 10)
    private String monedaDestino;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal tasa;

    @Column(nullable = false)
    private LocalDateTime actualizadoEn;
}
