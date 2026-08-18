package com.tienda.inventario.service;

import com.tienda.inventario.entity.TasaCambio;
import com.tienda.inventario.repository.TasaCambioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Mantiene en base de datos la ultima tasa de cambio conocida, actualizandola
 * periodicamente en vez de llamar a la API externa en cada venta (mas rapido
 * y evita quedar bloqueados si la API externa falla o tiene limite de uso).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TasaCambioService {

    private final TasaCambioRepository tasaCambioRepository;

    @Value("${app.exchangerate.api-url}")
    private String apiUrl;

    @Value("${app.exchangerate.moneda-base}")
    private String monedaBase;

    private final WebClient webClient = WebClient.builder().build();

    /**
     * Se ejecuta cada 6 horas. Ajusta la frecuencia segun lo que necesites;
     * las tasas de cambio no cambian tan seguido como para consultarlas en cada venta.
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 horas en milisegundos
    public void actualizarTasas() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> respuesta = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (respuesta == null || !respuesta.containsKey("rates")) {
                log.warn("Respuesta invalida de la API de tasas de cambio");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> rates = (Map<String, Object>) respuesta.get("rates");

            rates.forEach((moneda, valor) -> {
                BigDecimal tasa = new BigDecimal(valor.toString());
                guardarTasa(monedaBase, moneda, tasa);
            });

            log.info("Tasas de cambio actualizadas correctamente ({} monedas)", rates.size());
        } catch (Exception e) {
            log.error("Error al actualizar tasas de cambio: {}", e.getMessage());
            // No relanzamos la excepcion: si falla, el sistema sigue usando
            // la ultima tasa guardada en base de datos.
        }
    }

    // Nota: no se anota con @Transactional porque se llama desde dentro de la
    // misma clase (self-invocation), donde el proxy de Spring no intercepta
    // la llamada. No es un problema real: cada save() de JpaRepository ya es
    // transaccional por si mismo.
    private void guardarTasa(String origen, String destino, BigDecimal tasa) {
        TasaCambio tc = tasaCambioRepository
                .findTopByMonedaOrigenAndMonedaDestinoOrderByActualizadoEnDesc(origen, destino)
                .orElse(new TasaCambio());

        tc.setMonedaOrigen(origen);
        tc.setMonedaDestino(destino);
        tc.setTasa(tasa);
        tc.setActualizadoEn(LocalDateTime.now());
        tasaCambioRepository.save(tc);
    }

    /**
     * Devuelve la ultima tasa guardada. Si nunca se ha ejecutado el job,
     * intenta consultar la API en el momento como respaldo.
     */
    public BigDecimal obtenerTasa(String monedaDestino) {
        return tasaCambioRepository
                .findTopByMonedaOrigenAndMonedaDestinoOrderByActualizadoEnDesc(monedaBase, monedaDestino)
                .map(TasaCambio::getTasa)
                .orElseGet(() -> {
                    actualizarTasas();
                    return tasaCambioRepository
                            .findTopByMonedaOrigenAndMonedaDestinoOrderByActualizadoEnDesc(monedaBase, monedaDestino)
                            .map(TasaCambio::getTasa)
                            .orElseThrow(() -> new IllegalStateException(
                                    "No se pudo obtener la tasa de cambio para " + monedaDestino));
                });
    }
}
