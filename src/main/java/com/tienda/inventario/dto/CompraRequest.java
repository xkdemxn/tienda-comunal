package com.tienda.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompraRequest {

    @NotNull
    private Long proveedorId;

    @NotEmpty(message = "La compra debe tener al menos un producto")
    @Valid
    private List<ItemCompraRequest> items;
}
