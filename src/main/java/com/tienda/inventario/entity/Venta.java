package com.tienda.inventario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // quien realizo la venta

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalMonedaLocal;

    @Column(length = 10)
    private String monedaDestino; // ej: "USD" si se muestra tambien en otra moneda

    @Column(precision = 12, scale = 6)
    private BigDecimal tasaCambioUsada; // tasa aplicada en el momento de la venta

    @Column(precision = 12, scale = 2)
    private BigDecimal totalMonedaDestino; // total ya convertido, si aplica

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
    }
}
