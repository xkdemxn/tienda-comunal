package com.tienda.inventario.controller;

import com.tienda.inventario.dto.FiscalizacionAnualResponse;
import com.tienda.inventario.dto.FiscalizacionResponse;
import com.tienda.inventario.dto.ReporteVentasResponse;
import com.tienda.inventario.dto.FondoInicialRequest;
import com.tienda.inventario.service.ConfiguracionSistemaService;
import com.tienda.inventario.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final ConfiguracionSistemaService configuracionSistemaService;

    @GetMapping("/ventas")
    public ResponseEntity<ReporteVentasResponse> reporteVentas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(reporteService.generarReporte(desde, hasta));
    }

    @GetMapping("/fiscalizacion")
    public ResponseEntity<FiscalizacionResponse> reporteFiscalizacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(reporteService.generarReporteFiscalizacion(desde, hasta));
    }

    @GetMapping("/fiscalizacion/anual")
    public ResponseEntity<FiscalizacionAnualResponse> reporteFiscalizacionAnual(@RequestParam int anio) {
        return ResponseEntity.ok(reporteService.generarReporteFiscalizacionAnual(anio));
    }

    // Fondo inicial de caja: se confirma una sola vez (monto + desde que dia);
    // de ahi en adelante el arqueo calcula solo el efectivo esperado de
    // cualquier periodo. Solo ADMIN (todo /api/reportes/** lo es).
    @PutMapping("/fondo-inicial")
    public ResponseEntity<Void> guardarFondoInicial(@Valid @RequestBody FondoInicialRequest request) {
        configuracionSistemaService.guardarFondoInicial(request.monto(), request.fecha());
        return ResponseEntity.noContent().build();
    }
}
