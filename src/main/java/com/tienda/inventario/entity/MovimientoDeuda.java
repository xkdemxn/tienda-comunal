package com.tienda.inventario.entity;

import com.tienda.inventario.enums.TipoMovimientoDeuda;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro historico (kardex) de fiados y abonos de un deudor. El saldo
 * actual se calcula sumando FIADO y restando ABONO, igual que el stock se
 * calcula a partir de MovimientoInventario.
 */
@Entity
@Table(name = "movimientos_deuda")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoDeuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deudor_id", nullable = false)
    private Deudor deudor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimientoDeuda tipo;

    @Column(nullable = false)
    private BigDecimal monto; // siempre positivo, el signo lo da el "tipo"

    private String descripcion; // ej: "2 panes y una gaseosa", opcional

    // Detalle de productos cuando el fiado se registra eligiendo del stock
    // (en vez de un monto manual). Queda vacio en abonos y en fiados manuales.
    @OneToMany(mappedBy = "movimientoDeuda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleFiado> detalles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
    }
}
