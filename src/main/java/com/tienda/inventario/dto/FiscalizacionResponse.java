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
}
