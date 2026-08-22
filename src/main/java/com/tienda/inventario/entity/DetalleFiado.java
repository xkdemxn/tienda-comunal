package com.tienda.inventario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_fiado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleFiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_deuda_id", nullable = false)
    private MovimientoDeuda movimientoDeuda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario; // precio al momento del fiado (historico)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
