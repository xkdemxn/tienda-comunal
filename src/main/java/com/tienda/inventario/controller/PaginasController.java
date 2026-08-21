package com.tienda.inventario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Permite entrar a cada pagina sin el ".html" en la URL (ej: /dashboard en vez
// de /dashboard.html). "forward:" reenvia la peticion al archivo estatico real
// del lado del servidor, sin redirect, asi que la URL que ve el navegador queda
// limpia. Los .html originales siguen existiendo y tambien son accesibles.
@Controller
public class PaginasController {

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/productos")
    public String productos() {
        return "forward:/productos.html";
    }

    @GetMapping("/categorias")
    public String categorias() {
        return "forward:/categorias.html";
    }

    @GetMapping("/agregar-stock")
    public String agregarStock() {
        return "forward:/agregar-stock.html";
    }

    @GetMapping("/estadisticas")
    public String estadisticas() {
        return "forward:/estadisticas.html";
    }

    @GetMapping("/proveedores")
    public String proveedores() {
        return "forward:/proveedores.html";
    }

    @GetMapping("/compras")
    public String compras() {
        return "forward:/compras.html";
    }

    @GetMapping("/venta")
    public String venta() {
        return "forward:/venta.html";
    }

    @GetMapping("/historial-ventas")
    public String historialVentas() {
        return "forward:/historial-ventas.html";
    }

    @GetMapping("/fiscalizacion")
    public String fiscalizacion() {
        return "forward:/fiscalizacion.html";
    }
}
