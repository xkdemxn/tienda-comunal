package com.tienda.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VentaRequest {

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Valid
    private List<ItemVentaRequest> items;

    // Moneda a la que se quiere convertir el total, ej: "USD". Opcional.
    private String monedaDestino;
}
