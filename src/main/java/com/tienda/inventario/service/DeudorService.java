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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeudorService {

    private final DeudorRepository deudorRepository;
    private final MovimientoDeudaRepository movimientoDeudaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    // Por defecto solo los activos; con incluirInactivos tambien salen los
    // desactivados (para poder ver su historial o reactivarlos).
    public List<DeudorResponse> listar(boolean incluirInactivos) {
        List<Deudor> deudores = incluirInactivos
                ? deudorRepository.findAllByOrderByNombreAsc()
                : deudorRepository.findByActivoTrueOrderByNombreAsc();
        return deudores.stream()
                .map(d -> new DeudorResponse(d.getId(), d.getNombre(), d.getTelefono(),
                        calcularSaldo(d.getId()), d.getActivo()))
                .toList();
    }

    // Suma de lo que deben todos los deudores ahora mismo (no tiene rango de
    // fechas, es una foto del momento, igual que el stock actual). Incluye a
    // los desactivados: si alguno quedo con deuda, sigue sin estar pagada.
    public BigDecimal totalPendiente() {
        return deudorRepository.findAllByOrderByNombreAsc().stream()
                .map(d -> calcularSaldo(d.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Resumen de fiados: totales generales (deudores activos) + lo fiado y lo
    // cobrado (abonos) en el rango pedido, normalmente "hoy" (lo manda el
    // navegador para respetar la zona horaria de la tienda, no la del servidor).
    public ResumenDeudasResponse resumen(LocalDateTime desde, LocalDateTime hasta) {
        BigDecimal totalFiado = movimientoDeudaRepository.sumaPorTipo(TipoMovimientoDeuda.FIADO);
        BigDecimal totalCobrado = movimientoDeudaRepository.sumaPorTipo(TipoMovimientoDeuda.ABONO);
        int conDeuda = (int) deudorRepository.findAllByOrderByNombreAsc().stream()
                .filter(d -> calcularSaldo(d.getId()).signum() > 0)
                .count();
        return new ResumenDeudasResponse(
                totalFiado,
                totalCobrado,
                totalFiado.subtract(totalCobrado),
                conDeuda,
                movimientoDeudaRepository.sumaEnRangoPorTipo(TipoMovimientoDeuda.FIADO, desde, hasta),
                movimientoDeudaRepository.sumaEnRangoPorTipo(TipoMovimientoDeuda.ABONO, desde, hasta));
    }

    @Transactional
    public Deudor crear(DeudorRequest request) {
        Deudor deudor = new Deudor();
        deudor.setNombre(request.getNombre());
        deudor.setTelefono(request.getTelefono());
        deudor.setActivo(true);
        return deudorRepository.save(deudor);
    }

    // Nunca se borra un deudor (su historial de fiados/abonos debe conservarse):
    // solo se desactiva, y unicamente si esta al dia. Si aun debe, su deuda
    // desapareceria de "Deuda total pendiente" y del cierre de caja como si
    // se hubiera pagado, mientras el historial la sigue mostrando pendiente.
    @Transactional
    public void desactivar(Long id) {
        Deudor deudor = deudorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deudor no encontrado"));
        BigDecimal saldo = calcularSaldo(id);
        if (saldo.signum() > 0) {
            throw new IllegalArgumentException(deudor.getNombre() + " todavia debe $" + dinero(saldo)
                    + ". Cobra o registra su abono antes de desactivarlo");
        }
        deudor.setActivo(false);
        deudorRepository.save(deudor);
    }

    @Transactional
    public void activar(Long id) {
        Deudor deudor = deudorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deudor no encontrado"));
        deudor.setActivo(true);
        deudorRepository.save(deudor);
    }

    // Borra TODOS los movimientos de deuda (fiados y abonos) de TODOS los
    // deudores, dejando cada saldo en $0.00 - pero sin tocar la tabla de
    // deudores (nombres/telefonos quedan intactos). deleteAll() borra
    // entidad por entidad (no es un DELETE masivo crudo), asi que cascadea
    // correctamente a DetalleFiado via el cascade=ALL/orphanRemoval de
    // MovimientoDeuda.detalles. Accion irreversible, protegida ademas por
    // ADMIN + confirmacion explicita en el controller.
    @Transactional
    public void resetearDeudas() {
        movimientoDeudaRepository.deleteAll();
    }

    public List<MovimientoDeudaResponse> historial(Long deudorId) {
        return movimientoDeudaRepository.findByDeudorIdOrderByFechaDesc(deudorId).stream()
                .map(this::mapear)
                .toList();
    }

    // Fiados de TODOS los deudores en un rango de fechas - a diferencia de
    // "historial", que es de un solo deudor. Se usa para mostrar el fiado
    // por separado del efectivo en Estadisticas, Historial de ventas e
    // Inicio (esas paginas solo leen la tabla de Ventas, y un fiado no crea
    // una Venta, asi que sin esto quedaba invisible ahi aunque ya descuenta
    // stock y ya se refleja bien en Fiscalizacion).
    public List<FiadoEnRangoResponse> fiadoEnRango(LocalDateTime desde, LocalDateTime hasta) {
        // cache por deudor: el pagado de un fiado depende de TODO el historial
        // del deudor (no solo del rango pedido)
        Map<Long, Map<Long, BigDecimal>> pagadoPorDeudor = new HashMap<>();

        return movimientoDeudaRepository
                .findByTipoAndFechaBetweenOrderByFechaDesc(TipoMovimientoDeuda.FIADO, desde, hasta).stream()
                .map(m -> {
                    BigDecimal pagado = pagadoPorDeudor
                            .computeIfAbsent(m.getDeudor().getId(), this::pagadoPorFiado)
                            .getOrDefault(m.getId(), BigDecimal.ZERO);
                    String estado = pagado.compareTo(m.getMonto()) >= 0 ? "PAGADO"
                            : pagado.signum() > 0 ? "PARCIAL" : "PENDIENTE";
                    return new FiadoEnRangoResponse(
                            m.getId(),
                            m.getDeudor().getNombre(),
                            m.getMonto(),
                            m.getDescripcion(),
                            m.getFecha(),
                            m.getDetalles().stream()
                                    .map(d -> new DetalleFiadoResponse(
                                            d.getProducto().getNombre(), d.getCantidad(),
                                            d.getPrecioUnitario(), d.getSubtotal()))
                                    .toList(),
                            pagado.min(m.getMonto()),
                            estado);
                })
                .toList();
    }

    // Cuanto de cada fiado (id del fiado -> monto pagado) ya cubrieron los
    // abonos del deudor: los abonos se van aplicando a los fiados mas
    // antiguos primero, hasta agotarse.
    private Map<Long, BigDecimal> pagadoPorFiado(Long deudorId) {
        List<MovimientoDeuda> movimientos = movimientoDeudaRepository.findByDeudorId(deudorId).stream()
                .sorted(Comparator.comparing(MovimientoDeuda::getFecha).thenComparing(MovimientoDeuda::getId))
                .toList();

        BigDecimal abonosDisponibles = movimientos.stream()
                .filter(x -> x.getTipo() == TipoMovimientoDeuda.ABONO)
                .map(MovimientoDeuda::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> pagado = new HashMap<>();
        for (MovimientoDeuda fiado : movimientos) {
            if (fiado.getTipo() != TipoMovimientoDeuda.FIADO) continue;
            BigDecimal cubierto = abonosDisponibles.min(fiado.getMonto());
            pagado.put(fiado.getId(), cubierto);
            abonosDisponibles = abonosDisponibles.subtract(cubierto);
        }
        return pagado;
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

        // Un abono no puede pasar de lo que se debe: si no, el saldo quedaria
        // negativo (como si la tienda le debiera al cliente).
        BigDecimal saldo = calcularSaldo(deudorId);
        if (saldo.signum() <= 0) {
            throw new IllegalArgumentException(deudor.getNombre() + " no tiene deuda pendiente");
        }
        if (request.getMonto().compareTo(saldo) > 0) {
            BigDecimal exceso = request.getMonto().subtract(saldo);
            throw new IllegalArgumentException(deudor.getNombre() + " solo debe $" + dinero(saldo)
                    + ". El abono de $" + dinero(request.getMonto()) + " se pasa por $" + dinero(exceso));
        }

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

    private String dinero(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal calcularSaldo(Long deudorId) {
        return movimientoDeudaRepository.findByDeudorId(deudorId).stream()
                .map(m -> m.getTipo() == TipoMovimientoDeuda.FIADO ? m.getMonto() : m.getMonto().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
