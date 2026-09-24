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
    // Unidades que salieron por AJUSTE_NEGATIVO (caducado/merma) en el
    // periodo. Se muestra aparte de cantidadVendida a proposito: antes se
    // mezclaban y la ganancia reportada quedaba inflada con el margen de
    // unidades que en realidad se perdieron, no se vendieron.
    private Integer cantidadCaducada;
    private BigDecimal salidaConGanancia;
    private BigDecimal salidaPrecioMercado;
    private BigDecimal productoNetoExistentes;
    private BigDecimal sumaGanancia;
    // Costo de lo que se perdio por caducado/merma: cantidadCaducada x precio
    // de compra. Es plata invertida que no se va a recuperar vendiendo.
    private BigDecimal perdidaCaducados;
}
