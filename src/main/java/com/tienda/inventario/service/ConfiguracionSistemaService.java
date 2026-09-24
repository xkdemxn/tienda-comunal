package com.tienda.inventario.service;

import com.tienda.inventario.entity.ConfiguracionSistema;
import com.tienda.inventario.repository.ConfiguracionSistemaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConfiguracionSistemaService {

    private final ConfiguracionSistemaRepository repository;

    public ConfiguracionSistemaService(ConfiguracionSistemaRepository repository) {
        this.repository = repository;
    }

    public ConfiguracionSistema obtener() {
        return repository.findById(ConfiguracionSistema.ID_UNICO)
                .orElseGet(() -> repository.save(new ConfiguracionSistema()));
    }

    public boolean estaActiva() {
        return obtener().isSuscripcionActiva();
    }

    public ConfiguracionSistema actualizar(boolean activa, String mensaje) {
        ConfiguracionSistema config = obtener();
        config.setSuscripcionActiva(activa);
        if (mensaje != null && !mensaje.isBlank()) {
            config.setMensaje(mensaje);
        }
        config.setActualizadoEn(LocalDateTime.now());
        return repository.save(config);
    }

    public ConfiguracionSistema guardarFondoInicial(java.math.BigDecimal monto, java.time.LocalDate fecha) {
        ConfiguracionSistema config = obtener();
        config.setFondoInicialMonto(monto);
        config.setFondoInicialFecha(fecha);
        config.setActualizadoEn(LocalDateTime.now());
        return repository.save(config);
    }
}
