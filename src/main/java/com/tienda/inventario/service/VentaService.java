package com.tienda.inventario.service;

import com.tienda.inventario.dto.*;
import com.tienda.inventario.entity.*;
import com.tienda.inventario.enums.TipoMovimiento;
import com.tienda.inventario.repository.ProductoRepository;
import com.tienda.inventario.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final InventarioService inventarioService;
    private final TasaCambioService tasaCambioService;

    /**
     * Registra una venta completa: por cada item (codigo de barras + cantidad)
     * escaneado en el celular, reduce el stock y calcula el total.
     * Si viene monedaDestino, tambien calcula el total convertido con la tasa vigente.
     */
    @Transactional
    public VentaResponse registrarVenta(VentaRequest request, Usuario usuario) {
        Venta venta = new Venta();
        venta.setUsuario(usuario);

        BigDecimal totalLocal = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (ItemVentaRequest item : request.getItems()) {
            Producto producto = productoRepository.findByCodigoBarras(item.getCodigoBarras())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado para el codigo: " + item.getCodigoBarras()));

            // Esto reduce el stock y deja registro en el kardex; si no hay
            // suficiente stock o hay conflicto de concurrencia, lanza excepcion
            // y toda la transaccion de la venta se revierte (no queda a medias).
            inventarioService.reducirStock(item.getCodigoBarras(), item.getCantidad(),
                    TipoMovimiento.VENTA, usuario, "Venta");

            BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(item.getCantidad()));
            totalLocal = totalLocal.add(subtotal);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
        }

        venta.setDetalles(detalles);
        venta.setTotalMonedaLocal(totalLocal);

        if (request.getMonedaDestino() != null && !request.getMonedaDestino().isBlank()) {
            BigDecimal tasa = tasaCambioService.obtenerTasa(request.getMonedaDestino());
            venta.setMonedaDestino(request.getMonedaDestino());
            venta.setTasaCambioUsada(tasa);
            venta.setTotalMonedaDestino(totalLocal.multiply(tasa).setScale(2, RoundingMode.HALF_UP));
        }

        Venta guardada = ventaRepository.save(venta);
        return mapearResponse(guardada);
    }

    private VentaResponse mapearResponse(Venta venta) {
        List<DetalleVentaResponse> detallesResponse = venta.getDetalles().stream()
                .map(d -> new DetalleVentaResponse(
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()))
                .toList();

        return new VentaResponse(
                venta.getId(),
                venta.getTotalMonedaLocal(),
                venta.getMonedaDestino(),
                venta.getTasaCambioUsada(),
                venta.getTotalMonedaDestino(),
                venta.getFecha(),
                detallesResponse
        );
    }
}
