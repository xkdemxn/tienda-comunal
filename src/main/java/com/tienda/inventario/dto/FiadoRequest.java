package com.tienda.inventario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FiadoRequest {

    @NotEmpty
    @Valid
    private List<ItemFiadoRequest> items;
}
