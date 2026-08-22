package com.tienda.inventario.service;

import com.tienda.inventario.dto.*;
import com.tienda.inventario.entity.DetalleFiado;
import com.tienda.inventario.entity.Deudor;
import com.tienda.inventario.entity.MovimientoDeuda;
import com.tienda.inventario.entity.Producto;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.enums.TipoMovimiento;
import com.tienda.inventario.enums.TipoMovimientoDeuda;
import com.tienda.inventario.repository.DeudorRepository;
import com.tienda.inventario.repository.MovimientoDeudaRepository;
import com.tienda.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeudorService {

    private final DeudorRepository deudorRepository;
    private final MovimientoDeudaRepository movimientoDeudaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    public List<DeudorResponse> listar() {
        return deudorRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(d -> new DeudorResponse(d.getId(), d.getNombre(), d.getTelefono(), calcularSaldo(d.getId())))
                .toList();
    }

    // Suma de lo que deben todos los deudores activos ahora mismo (no tiene
    // rango de fechas, es una foto del momento, igual que el stock actual).
    public BigDecimal totalPendiente() {
        return deudorRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(d -> calcularSaldo(d.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Deudor crear(DeudorRequest request) {
        Deudor deudor = new Deudor();
        deudor.setNombre(request.getNombre());
        deudor.setTelefono(request.getTelefono());
        deudor.setActivo(true);
        return deudorRepository.save(deudor);
    }

    @Transactional
    public void desactivar(Long id) {
        Deudor deudor = deudorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deudor no encontrado"));
        deudor.setActivo(false);
        deudorRepository.save(deudor);
    }

    public List<MovimientoDeudaResponse> historial(Long deudorId) {
        return movimientoDeudaRepository.findByDeudorIdOrderByFechaDesc(deudorId).stream()
                .map(this::mapear)
                .toList();
    }

    // Fiado manual: un monto y una descripcion libre, sin tocar stock (para
    // casos que no son productos del catalogo, ej. un vuelto fiado).
    @Transactional
    public MovimientoDeudaResponse registrarFiado(Long deudorId, MovimientoDeudaRequest request, Usuario usuario) {
        Deudor deudor = deudorRepository.findById(deudorId)
                .orElseThrow(() -> new IllegalArgumentException("Deudor no encontrado"));

        MovimientoDeuda movimiento = new MovimientoDeuda();
        movimiento.setDeudor(deudor);
        movimiento.setTipo(TipoMovimientoDeuda.FIADO);
        movimiento.setMonto(request.getMonto());
        movimiento.setDescripcion(request.getDescripcion());
        movimiento.setUsuario(usuario);

        movimiento = movimientoDeudaRepository.save(movimiento);
        return mapear(movimiento);
    }

    // Fiado eligiendo productos del stock (como una venta, pero sin cobrar):
    // reduce el stock de cada item y deja el detalle con fecha, igual que una
    // venta normal.
    @Transactional
    public MovimientoDeudaResponse registrarFiadoConProductos(Long deudorId, FiadoRequest request, Usuario usuario) {
        Deudor deudor = deudorRepository.findById(deudorId)
                .orElseThrow(() -> new IllegalArgumentException("Deudor no encontrado"));

        MovimientoDeuda movimiento = new MovimientoDeuda();
        movimiento.setDeudor(deudor);
        movimiento.setTipo(TipoMovimientoDeuda.FIADO);
        movimiento.setUsuario(usuario);

        BigDecimal total = BigDecimal.ZERO;
        List<DetalleFiado> detalles = new ArrayList<>();
        List<String> resumen = new ArrayList<>();

        for (ItemFiadoRequest item : request.getItems()) {
            Producto producto = productoRepository.findByCodigoBarras(item.getCodigoBarras())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado para el codigo: " + item.getCodigoBarras()));

            // Reduce el stock y deja registro en el kardex, igual que una venta;
            // si no hay stock suficiente, se revierte todo el fiado.
            inventarioService.reducirStock(item.getCodigoBarras(), item.getCantidad(),
                    TipoMovimiento.VENTA, usuario, "Fiado a " + deudor.getNombre());

            BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);
            resumen.add(item.getCantidad() + "x " + producto.getNombre());

            DetalleFiado detalle = new DetalleFiado();
            detalle.setMovimientoDeuda(movimiento);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
        }

        movimiento.setMonto(total);
        movimiento.setDescripcion(String.join(", ", resumen));
        movimiento.setDetalles(detalles);

        movimiento = movimientoDeudaRepository.save(movimiento);
        return mapear(movimiento);
    }

    @Transactional
    public MovimientoDeudaResponse registrarAbono(Long deudorId, MovimientoDeudaRequest request, Usuario usuario) {
        Deudor deudor = deudorRepository.findById(deudorId)
                .orElseThrow(() -> new IllegalArgumentException("Deudor no encontrado"));

        MovimientoDeuda movimiento = new MovimientoDeuda();
        movimiento.setDeudor(deudor);
        movimiento.setTipo(TipoMovimientoDeuda.ABONO);
        movimiento.setMonto(request.getMonto());
        movimiento.setDescripcion(request.getDescripcion());
        movimiento.setUsuario(usuario);

        movimiento = movimientoDeudaRepository.save(movimiento);
        return mapear(movimiento);
    }

    private MovimientoDeudaResponse mapear(MovimientoDeuda m) {
        List<DetalleFiadoResponse> detalles = m.getDetalles().stream()
                .map(d -> new DetalleFiadoResponse(d.getProducto().getNombre(), d.getCantidad(),
                        d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        return new MovimientoDeudaResponse(m.getId(), m.getTipo(), m.getMonto(), m.getDescripcion(),
                m.getFecha(), detalles);
    }

    private BigDecimal calcularSaldo(Long deudorId) {
        return movimientoDeudaRepository.findByDeudorId(deudorId).stream()
                .map(m -> m.getTipo() == TipoMovimientoDeuda.FIADO ? m.getMonto() : m.getMonto().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
