package com.tienda.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductoStockBajoDto {
    private String nombre;
    private String codigoBarras;
    private Integer stockActual;
    private Integer stockMinimo;
}
