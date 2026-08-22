package com.tienda.inventario.service;

import com.tienda.inventario.dto.DeudorRequest;
import com.tienda.inventario.dto.DeudorResponse;
import com.tienda.inventario.dto.MovimientoDeudaRequest;
import com.tienda.inventario.dto.MovimientoDeudaResponse;
import com.tienda.inventario.entity.Deudor;
import com.tienda.inventario.entity.MovimientoDeuda;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.enums.TipoMovimientoDeuda;
import com.tienda.inventario.repository.DeudorRepository;
import com.tienda.inventario.repository.MovimientoDeudaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeudorService {

    private final DeudorRepository deudorRepository;
    private final MovimientoDeudaRepository movimientoDeudaRepository;

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
                .map(m -> new MovimientoDeudaResponse(m.getId(), m.getTipo(), m.getMonto(), m.getDescripcion(), m.getFecha()))
                .toList();
    }

    @Transactional
    public MovimientoDeudaResponse registrarFiado(Long deudorId, MovimientoDeudaRequest request, Usuario usuario) {
        return registrarMovimiento(deudorId, request, TipoMovimientoDeuda.FIADO, usuario);
    }

    @Transactional
    public MovimientoDeudaResponse registrarAbono(Long deudorId, MovimientoDeudaRequest request, Usuario usuario) {
        return registrarMovimiento(deudorId, request, TipoMovimientoDeuda.ABONO, usuario);
    }

    private MovimientoDeudaResponse registrarMovimiento(Long deudorId, MovimientoDeudaRequest request,
                                                          TipoMovimientoDeuda tipo, Usuario usuario) {
        Deudor deudor = deudorRepository.findById(deudorId)
                .orElseThrow(() -> new IllegalArgumentException("Deudor no encontrado"));

        MovimientoDeuda movimiento = new MovimientoDeuda();
        movimiento.setDeudor(deudor);
        movimiento.setTipo(tipo);
        movimiento.setMonto(request.getMonto());
        movimiento.setDescripcion(request.getDescripcion());
        movimiento.setUsuario(usuario);

        movimiento = movimientoDeudaRepository.save(movimiento);

        return new MovimientoDeudaResponse(movimiento.getId(), movimiento.getTipo(), movimiento.getMonto(),
                movimiento.getDescripcion(), movimiento.getFecha());
    }

    private BigDecimal calcularSaldo(Long deudorId) {
        return movimientoDeudaRepository.findByDeudorId(deudorId).stream()
                .map(m -> m.getTipo() == TipoMovimientoDeuda.FIADO ? m.getMonto() : m.getMonto().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
