package com.tienda.inventario.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Borra ventas y compras (cabeceras); Postgres arrastra en cascada sus
     * detalles, el historial de movimientos de inventario y los productos
     * por las llaves foraneas. Usuarios, roles, categorias, proveedores y
     * tasas de cambio no se tocan.
     */
    @Transactional
    public void resetProductosYVentas() {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE ventas, compras, productos RESTART IDENTITY CASCADE"
        ).executeUpdate();
    }
}
