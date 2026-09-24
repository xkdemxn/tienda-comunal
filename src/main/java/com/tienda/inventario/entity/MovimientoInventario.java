package com.tienda.inventario.entity;

import com.tienda.inventario.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro historico (kardex) de todo cambio de stock.
 * Sirve para auditoria y para los reportes/analisis.
 */
@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad; // siempre positivo, el signo lo da el "tipo"

    @Column(nullable = false)
    private Integer stockResultante; // stock del producto luego del movimiento

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String motivo; // usado sobre todo en ajustes manuales

    // true = baja hecha para corregir un error de registro (no es un producto
    // caducado/danado): no debe aparecer en Caducados ni contar como perdida.
    // Nullable a proposito: la tabla ya tiene filas y "null" se lee como false.
    private Boolean correccion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
    }
}
