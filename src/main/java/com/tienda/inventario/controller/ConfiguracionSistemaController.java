package com.tienda.inventario.controller;

import com.tienda.inventario.dto.ActualizarSuscripcionRequest;
import com.tienda.inventario.entity.ConfiguracionSistema;
import com.tienda.inventario.service.ConfiguracionSistemaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Endpoint fuera del control de roles del tiendero: la reactivacion/suspension
// se protege con una clave maestra propia (app.clave-maestra), no con el rol
// ADMIN de la tienda, para que el dueno de la tienda no pueda tocar esto.
@RestController
@RequestMapping("/api/sistema")
public class ConfiguracionSistemaController {

    private final ConfiguracionSistemaService service;

    @Value("${app.clave-maestra}")
    private String claveMaestra;

    public ConfiguracionSistemaController(ConfiguracionSistemaService service) {
        this.service = service;
    }

    @GetMapping("/estado")
    public Map<String, Object> estado() {
        ConfiguracionSistema config = service.obtener();
        return Map.of(
                "activa", config.isSuscripcionActiva(),
                "mensaje", config.getMensaje()
        );
    }

    @PutMapping("/suscripcion")
    public ResponseEntity<?> actualizar(@RequestBody ActualizarSuscripcionRequest body) {
        if (body.claveAdmin() == null || !body.claveAdmin().equals(claveMaestra)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", "Clave invalida"));
        }
        ConfiguracionSistema config = service.actualizar(body.activa(), body.mensaje());
        return ResponseEntity.ok(Map.of(
                "activa", config.isSuscripcionActiva(),
                "mensaje", config.getMensaje()
        ));
    }
}
