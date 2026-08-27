package com.tienda.inventario.dto;

import java.math.BigDecimal;

// ganancia/ventasBruto en null = mes que todavia no ha llegado (dentro del
// anio actual) - no hay datos que mostrar, a diferencia de un mes ya pasado
// con ganancia $0.
public record FiscalizacionMesDto(int mes, String nombreMes, BigDecimal ganancia, BigDecimal ventasBruto) {
}
