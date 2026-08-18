package com.tienda.inventario.config;

import com.tienda.inventario.entity.Rol;
import com.tienda.inventario.enums.RolNombre;
import com.tienda.inventario.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Se ejecuta al arrancar la aplicacion y crea los roles base (ADMIN, VENDEDOR)
 * si todavia no existen en la base de datos. Sin esto, el registro de usuarios fallaria
 * porque el rol no existiria para asociarlo.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        for (RolNombre nombre : RolNombre.values()) {
            rolRepository.findByNombre(nombre).orElseGet(() -> {
                Rol rol = new Rol();
                rol.setNombre(nombre);
                return rolRepository.save(rol);
            });
        }
    }
}
