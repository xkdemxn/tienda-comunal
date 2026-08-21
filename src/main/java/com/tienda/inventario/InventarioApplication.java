package com.tienda.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling // necesario para el job que actualiza la tasa de cambio
public class InventarioApplication {

    public static void main(String[] args) {
        // Render corre el servidor en UTC. Como todo el proyecto usa LocalDateTime.now()
        // (sin zona horaria), esto fija la zona del JVM a Ecuador para que las fechas
        // guardadas y mostradas (ventas, compras, movimientos, etc.) sean la hora real.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Guayaquil"));
        SpringApplication.run(InventarioApplication.class, args);
    }

}
