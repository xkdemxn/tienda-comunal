package com.tienda.inventario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String codigoBarras; // EAN/UPC leido por la camara

    @Column(nullable = false, length = 150)
    private String nombre;

    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(nullable = false)
    private Integer stockActual = 0;

    @Column(nullable = false)
    private Integer stockMinimo = 5; // para alertas de stock bajo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(nullable = false)
    private Boolean activo = true;

    // Version para bloqueo optimista: evita que dos ventas simultaneas
    // dejen el stock inconsistente cuando se escanea el mismo producto
    // casi al mismo tiempo desde dos dispositivos distintos.
    @Version
    private Long version;

    private LocalDateTime actualizadoEn;

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
