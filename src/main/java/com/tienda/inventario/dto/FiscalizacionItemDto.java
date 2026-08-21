package com.tienda.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class FiscalizacionItemDto {
    private String nombre;
    private String codigoBarras;

    private Integer saldoAnterior;
    private Integer entradaDelMes;
    private Integer total;
    private Integer existenciaActual;
    private BigDecimal valorExistencia;
    private Integer cantidadVendida;
    private BigDecimal salidaConGanancia;
    private BigDecimal salidaPrecioMercado;
    private BigDecimal productoNetoExistentes;
    private BigDecimal sumaGanancia;
}
