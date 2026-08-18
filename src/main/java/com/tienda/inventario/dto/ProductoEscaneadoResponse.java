package com.tienda.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ProductoEscaneadoResponse {
    private Long productoId;
    private String nombre;
    private String codigoBarras;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private boolean stockSuficiente;
}
