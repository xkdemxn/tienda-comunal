package com.tienda.inventario.service;

import com.tienda.inventario.dto.ProductoEscaneadoResponse;
import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EscaneoService {

    private final ProductoRepository productoRepository;

    /**
     * Se llama cada vez que la camara del celular lee un codigo de barras.
     * Devuelve los datos del producto para mostrarlos en pantalla antes de confirmar la venta.
     */
    public ProductoEscaneadoResponse buscarPorCodigo(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Codigo de barras no reconocido: " + codigoBarras));

        return new ProductoEscaneadoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getCodigoBarras(),
                producto.getPrecioVenta(),
                producto.getStockActual(),
                producto.getStockActual() > 0
        );
    }
}
