package com.tienda.inventario.security;

import com.tienda.inventario.service.ConfiguracionSistemaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// No es @Component a proposito: se registra a mano en SecurityConfig antes que
// cualquier chequeo de autenticacion, para que el bloqueo aplique aunque el
// request no traiga token (incluye el propio login).
public class SuscripcionFilter extends OncePerRequestFilter {

    private final ConfiguracionSistemaService configuracionSistemaService;

    public SuscripcionFilter(ConfiguracionSistemaService configuracionSistemaService) {
        this.configuracionSistemaService = configuracionSistemaService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean rutaDeGestion = path.equals("/api/sistema/estado") || path.equals("/api/sistema/suscripcion");

        if (!path.startsWith("/api/") || rutaDeGestion || configuracionSistemaService.estaActiva()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String mensaje = configuracionSistemaService.obtener().getMensaje();
        response.getWriter().write("{\"mensaje\":\"" + mensaje.replace("\"", "'") + "\"}");
    }
}
