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
    private String codigoBarras; // EAN/UPC leido por la camara, identifica UNA unidad

    // Codigo de barras del paquete/caja (ej: la paca de 12 botellas), opcional.
    // El stock siempre se lleva en unidades: escanear este codigo suma
    // "unidadesPorCaja" de una sola vez en vez de escanear unidad por unidad.
    @Column(unique = true, length = 64)
    private String codigoBarrasCaja;

    private Integer unidadesPorCaja;

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

    // Ej: carne, que se cobra segun lo que pese en la balanza, no por unidad
    // entera. precioVenta se interpreta como "precio por libra" en ese caso.
    // No se lleva stock exacto para estos productos (ver VentaService).
    // OJO: nullable a proposito (no "nullable = false"). Si fuera obligatoria,
    // el ALTER TABLE para agregarla falla en una tabla que ya tiene productos
    // guardados (Postgres no permite una columna NOT NULL nueva sin default
    // en una tabla con filas existentes) - el codigo ya trata null como false
    // en todos lados (ProductoService, VentaService), asi que es seguro.
    private Boolean vendidoPorPeso = false;

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
