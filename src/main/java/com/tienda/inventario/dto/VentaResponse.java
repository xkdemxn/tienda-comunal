package com.tienda.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class VentaResponse {
    private Long ventaId;
    private BigDecimal totalMonedaLocal;
    private String monedaDestino;
    private BigDecimal tasaCambioUsada;
    private BigDecimal totalMonedaDestino;
    private LocalDateTime fecha;
    private List<DetalleVentaResponse> detalles;
}
