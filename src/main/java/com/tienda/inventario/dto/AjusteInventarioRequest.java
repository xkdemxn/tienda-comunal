package com.tienda.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjusteInventarioRequest {

    @NotBlank
    private String codigoBarras;

    @Min(1)
    private Integer cantidad;

    @NotBlank
    private String motivo;

    @NotNull
    private Boolean esPositivo; // true = suma stock, false = resta stock

    // Solo aplica a entradas (esPositivo=true): si viene en true, la entrada se
    // registra como una compra directa (sin proveedor) pagada con plata de la
    // caja, para que el arqueo la reste del efectivo esperado. Opcional.
    private Boolean registrarComoCompra;

    // Solo aplica a bajas (esPositivo=false): si viene en true, la baja corrige
    // un error de registro de una compra directa, asi que ademas de bajar el
    // stock se devuelve ese costo al efectivo esperado del arqueo (registra una
    // compra negativa). Sin esto, la baja no toca la caja (ej: merma/caducado).
    private Boolean revertirCompra;

    // Solo aplica a bajas: true = corrige un error de registro (no es un
    // producto caducado/danado), asi que no aparece en Caducados ni cuenta como
    // perdida en Fiscalizacion.
    private Boolean esCorreccion;
}
