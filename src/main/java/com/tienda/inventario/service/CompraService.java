package com.tienda.inventario.service;

import com.tienda.inventario.dto.CompraRequest;
import com.tienda.inventario.dto.ItemCompraRequest;
import com.tienda.inventario.entity.*;
import com.tienda.inventario.enums.TipoMovimiento;
import com.tienda.inventario.repository.CompraRepository;
import com.tienda.inventario.repository.ProductoRepository;
import com.tienda.inventario.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final CompraRepository compraRepository;
    private final InventarioService inventarioService;

    public static final String PROVEEDOR_DIRECTO = "Compra directa (sin proveedor)";

    /**
     * Entrada de stock pagada con plata de la caja, sin proveedor (ej: un
     * producto suelto): suma el stock igual que una compra y registra el
     * costo (precio de compra del producto x cantidad) para que el arqueo de
     * caja lo reste del efectivo esperado. Si el producto no tiene precio de
     * compra, el costo queda en 0.
     */
    @Transactional
    public Compra registrarCompraDirecta(String codigoBarras, int cantidad, Usuario usuario) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado para el codigo: " + codigoBarras));

        Proveedor proveedor = proveedorDirecto();

        ItemCompraRequest item = new ItemCompraRequest();
        item.setCodigoBarras(codigoBarras);
        item.setCantidad(cantidad);
        item.setCostoUnitario(producto.getPrecioCompra() != null ? producto.getPrecioCompra() : BigDecimal.ZERO);

        CompraRequest request = new CompraRequest();
        request.setProveedorId(proveedor.getId());
        request.setItems(List.of(item));
        return registrarCompra(request, usuario);
    }

    private Proveedor proveedorDirecto() {
        return proveedorRepository.findFirstByNombre(PROVEEDOR_DIRECTO)
                .orElseGet(() -> {
                    Proveedor nuevo = new Proveedor();
                    nuevo.setNombre(PROVEEDOR_DIRECTO);
                    return proveedorRepository.save(nuevo);
                });
    }

    /**
     * Corrige un error de registro de una compra directa: quita las unidades del
     * stock y registra una compra NEGATIVA por el mismo costo, asi la plata
     * vuelve al efectivo esperado del arqueo. Si el producto no tiene precio de
     * compra no hubo costo que devolver y solo se baja el stock.
     */
    @Transactional
    public Producto registrarCorreccionDeCompraDirecta(String codigoBarras, int cantidad, Usuario usuario) {
        Producto producto = inventarioService.reducirStock(codigoBarras, cantidad,
                TipoMovimiento.AJUSTE_NEGATIVO, usuario, "Correccion de error de registro (compra directa)", true);

        BigDecimal costo = producto.getPrecioCompra();
        if (costo == null || costo.signum() <= 0) {
            return producto;
        }

        BigDecimal subtotal = costo.multiply(BigDecimal.valueOf(cantidad)).negate();

        Compra compra = new Compra();
        compra.setProveedor(proveedorDirecto());
        compra.setUsuario(usuario);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setCompra(compra);
        detalle.setProducto(producto);
        detalle.setCantidad(-cantidad);
        detalle.setCostoUnitario(costo);
        detalle.setSubtotal(subtotal);

        compra.setDetalles(new ArrayList<>(List.of(detalle)));
        compra.setTotal(subtotal);
        compraRepository.save(compra);
        return producto;
    }

    /**
     * Registra una compra a proveedor: por cada item aumenta el stock
     * del producto correspondiente y deja registro en el kardex.
     */
    @Transactional
    public Compra registrarCompra(CompraRequest request, Usuario usuario) {
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);

        BigDecimal total = BigDecimal.ZERO;
        List<DetalleCompra> detalles = new ArrayList<>();

        for (ItemCompraRequest item : request.getItems()) {
            Producto producto = productoRepository.findByCodigoBarras(item.getCodigoBarras())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado para el codigo: " + item.getCodigoBarras()));

            inventarioService.aumentarStock(item.getCodigoBarras(), item.getCantidad(),
                    TipoMovimiento.COMPRA, usuario, "Compra a proveedor: " + proveedor.getNombre());

            BigDecimal subtotal = item.getCostoUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compra);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setCostoUnitario(item.getCostoUnitario());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
        }

        compra.setDetalles(detalles);
        compra.setTotal(total);

        return compraRepository.save(compra);
    }
}
