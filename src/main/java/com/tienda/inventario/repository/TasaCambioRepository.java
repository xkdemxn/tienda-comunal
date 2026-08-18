package com.tienda.inventario.repository;

import com.tienda.inventario.entity.TasaCambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasaCambioRepository extends JpaRepository<TasaCambio, Long> {
    Optional<TasaCambio> findTopByMonedaOrigenAndMonedaDestinoOrderByActualizadoEnDesc(
            String monedaOrigen, String monedaDestino);
}
