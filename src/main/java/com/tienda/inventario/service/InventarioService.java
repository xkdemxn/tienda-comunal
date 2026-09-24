package com.tienda.inventario.service;

import com.tienda.inventario.dto.MovimientoInventarioResponse;
import com.tienda.inventario.entity.MovimientoInventario;
import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.enums.TipoMovimiento;
import com.tienda.inventario.repository.MovimientoInventarioRepository;
import com.tienda.inventario.repository.ProductoRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Punto UNICO por donde debe pasar cualquier cambio de stock.
 * Asi garantizamos que siempre quede registrado en el kardex (MovimientoInventario)
 * y que el control de concurrencia (via @Version en Producto) se aplique siempre.
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    /**
     * Reduce stock (venta o ajuste negativo). Lanza excepcion si no hay stock suficiente.
     * @Transactional con reintento simple ante choque de version optimista.
     */
    @Transactional
    public Producto reducirStock(String codigoBarras, int cantidad, TipoMovimiento tipo,
                                  Usuario usuario, String motivo) {
        return reducirStock(codigoBarras, cantidad, tipo, usuario, motivo, false);
    }

    /**
     * Igual que la anterior; con correccion=true la baja queda marcada como
     * correccion de un error de registro (no cuenta como caducado ni perdida).
     */
    @Transactional
    public Producto reducirStock(String codigoBarras, int cantidad, TipoMovimiento tipo,
                                  Usuario usuario, String motivo, boolean correccion) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un producto con el codigo de barras: " + codigoBarras));

        if (producto.getStockActual() < cantidad) {
            throw new IllegalStateException(
                    "Stock insuficiente para '" + producto.getNombre() + "'. Disponible: "
                            + producto.getStockActual() + ", solicitado: " + cantidad);
        }

        producto.setStockActual(producto.getStockActual() - cantidad);

        try {
            productoRepository.saveAndFlush(producto);
        } catch (OptimisticLockException e) {
            // Otro proceso modifico el mismo producto al mismo tiempo (ej. dos cajas
            // escaneando el mismo articulo). Se informa para que el cliente reintente
            // la operacion con el stock ya actualizado.
            throw new IllegalStateException(
                    "El stock de '" + producto.getNombre() + "' cambio simultaneamente, intenta de nuevo");
        }

        registrarMovimiento(producto, tipo, cantidad, usuario, motivo, correccion);
        return producto;
    }

    /**
     * Aumenta stock (compra, devolucion o ajuste positivo).
     */
    @Transactional
    public Producto aumentarStock(String codigoBarras, int cantidad, TipoMovimiento tipo,
                                   Usuario usuario, String motivo) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un producto con el codigo de barras: " + codigoBarras));

        producto.setStockActual(producto.getStockActual() + cantidad);

        try {
            productoRepository.saveAndFlush(producto);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException(
                    "El stock de '" + producto.getNombre() + "' cambio simultaneamente, intenta de nuevo");
        }

        registrarMovimiento(producto, tipo, cantidad, usuario, motivo, false);
        return producto;
    }

    private void registrarMovimiento(Producto producto, TipoMovimiento tipo, int cantidad,
                                      Usuario usuario, String motivo, boolean correccion) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setStockResultante(producto.getStockActual());
        mov.setUsuario(usuario);
        mov.setMotivo(motivo);
        mov.setCorreccion(correccion);
        movimientoRepository.save(mov);
    }

    // Historial de un tipo de movimiento en un rango (ej: AJUSTE_NEGATIVO
    // para ver productos caducados/mermas registrados).
    public List<MovimientoInventarioResponse> listarPorTipo(TipoMovimiento tipo, LocalDateTime desde, LocalDateTime hasta) {
        return movimientoRepository.findByTipoAndFechaBetweenOrderByFechaDesc(tipo, desde, hasta).stream()
                // las correcciones de errores de registro no son caducados/mermas
                .filter(m -> !Boolean.TRUE.equals(m.getCorreccion()))
                .map(m -> new MovimientoInventarioResponse(
                        m.getId(), m.getProducto().getNombre(), m.getProducto().getCodigoBarras(),
                        m.getCantidad(), m.getMotivo(), m.getFecha()))
                .toList();
    }
}
