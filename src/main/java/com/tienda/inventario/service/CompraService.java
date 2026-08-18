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
