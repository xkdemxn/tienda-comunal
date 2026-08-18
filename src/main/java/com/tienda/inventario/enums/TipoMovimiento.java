package com.tienda.inventario.enums;

public enum TipoMovimiento {
    VENTA,        // reduce stock
    COMPRA,       // aumenta stock (entrada de proveedor)
    DEVOLUCION,   // aumenta stock (cliente devuelve producto)
    AJUSTE_POSITIVO, // correccion manual, aumenta stock
    AJUSTE_NEGATIVO  // correccion manual (merma, dano, perdida), reduce stock
}
