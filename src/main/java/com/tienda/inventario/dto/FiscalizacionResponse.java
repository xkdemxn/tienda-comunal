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
public class FiscalizacionResponse {
    // nombre de categoria (o "SIN CATEGORIA") -> items de esa seccion
    private Map<String, List<FiscalizacionItemDto>> porCategoria;
    private BigDecimal totalGeneralGanancia;

    // Suma literal de Venta.totalMonedaLocal en el rango: todo el dinero que
    // entro por ventas, sin restar costo ni gastos (a diferencia de "ganancia").
    private BigDecimal totalVentasBruto;

    // Cierre de caja del periodo:
    //   totalGastos: gastos registrados en el rango (pago a trabajador, etc.)
    //   q: totalGeneralGanancia - totalGastos
    //   deudasPendientes: lo que deben ahora mismo todos los deudores activos
    // "Ingreso en efectivo" NO va aca porque es un conteo fisico manual, no
    // sale de ninguna tabla - el frontend lo pide como input y calcula
    // "ingresa al siguiente mes" = q + ingresoEnEfectivo + deudasPendientes.
    private BigDecimal totalGastos;
    private BigDecimal q;
    private BigDecimal deudasPendientes;

    // Productos "vendidos por peso" (ej: carne): quedan fuera de porCategoria
    // porque no tienen kardex (no se les lleva stock), asi que el informe por
    // categoria siempre les daria 0 en todo. Su ganancia real se calcula
    // directo de las ventas y ya esta sumada dentro de totalGeneralGanancia.
    private List<FiscalizacionPorPesoDto> productosPorPeso;
    // Dinero que salio por compras a proveedores en el periodo (suma del total
    // de cada Compra). Es solo informativo: NO se resta de la ganancia, porque
    // el costo ya entra por el margen al vender (restarlo tambien lo contaria
    // dos veces). Lo que se agrega por "Agregar stock" no tiene costo, asi que
    // no aparece aca.
    private BigDecimal totalCompras;
    private long cantidadCompras;
}
