package com.tienda.inventario.dto;

import java.util.List;

public record FiscalizacionAnualResponse(int anio, List<FiscalizacionMesDto> meses) {
}
