package com.tienda.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ReporteVentasResponse {
    private BigDecimal totalVendido;
    private long cantidadVentas;
    private Map<String, Integer> productosMasVendidos; // nombre -> cantidad total vendida
    private List<ProductoStockBajoDto> productosStockBajo;
}
