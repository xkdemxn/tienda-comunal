package com.tienda.inventario.service;

import com.tienda.inventario.dto.GastoRequest;
import com.tienda.inventario.dto.GastoResponse;
import com.tienda.inventario.entity.Gasto;
import com.tienda.inventario.entity.Usuario;
import com.tienda.inventario.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoRepository gastoRepository;

    @Transactional
    public GastoResponse registrar(GastoRequest request, Usuario usuario) {
        Gasto gasto = new Gasto();
        gasto.setDescripcion(request.getDescripcion());
        gasto.setMonto(request.getMonto());
        gasto.setUsuario(usuario);
        gasto = gastoRepository.save(gasto);
        return mapear(gasto);
    }

    public List<GastoResponse> listar(LocalDateTime desde, LocalDateTime hasta) {
        return gastoRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta).stream()
                .map(this::mapear)
                .toList();
    }

    public BigDecimal totalEnRango(LocalDateTime desde, LocalDateTime hasta) {
        return gastoRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta).stream()
                .map(Gasto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void eliminar(Long id) {
        gastoRepository.deleteById(id);
    }

    private GastoResponse mapear(Gasto g) {
        return new GastoResponse(g.getId(), g.getDescripcion(), g.getMonto(), g.getFecha());
    }
}
