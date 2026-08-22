package com.tienda.inventario.config;

import com.tienda.inventario.security.JwtAuthFilter;
import com.tienda.inventario.security.SuscripcionFilter;
import com.tienda.inventario.service.ConfiguracionSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService usuarioDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final ConfiguracionSistemaService configuracionSistemaService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // login/registro publicos (rutas explicitas: /api/auth/password NO
                        // es publica, requiere estar logueado - queda cubierta mas abajo
                        // por anyRequest().authenticated())
                        .requestMatchers("/api/auth/login", "/api/auth/registro").permitAll()
                        // estado/gestion de suscripcion: protegido por clave maestra propia,
                        // no por rol, para que sea independiente del login del tiendero
                        .requestMatchers("/api/sistema/**").permitAll()
                        // frontend estatico (el login ocurre dentro de la pagina via JS).
                        // Se permiten las rutas limpias (sin .html, ver PaginasController) y
                        // tambien los .html originales por compatibilidad con links viejos.
                        .requestMatchers("/", "/login.html", "/dashboard.html", "/productos.html",
                                "/categorias.html", "/agregar-stock.html", "/estadisticas.html",
                                "/proveedores.html", "/compras.html", "/venta.html", "/fiscalizacion.html",
                                "/historial-ventas.html", "/deudores.html", "/caducados.html",
                                "/login", "/dashboard", "/productos", "/categorias", "/agregar-stock",
                                "/estadisticas", "/proveedores", "/compras", "/venta", "/fiscalizacion",
                                "/historial-ventas", "/deudores", "/caducados",
                                "/*.js", "/*.css", "/favicon.ico", "/img/**").permitAll()
                        // ver productos (util para catalogo publico si se necesita)
                        .requestMatchers("GET", "/api/productos/**").hasAnyRole("ADMIN", "VENDEDOR")
                        // registrar ventas: admin y vendedor
                        .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "VENDEDOR")
                        // fiar/abonar: admin y vendedor (se hace en el mostrador, como una venta)
                        .requestMatchers("/api/deudores/**").hasAnyRole("ADMIN", "VENDEDOR")
                        // todo lo de inventario/compras/reportes/admin solo admin
                        .requestMatchers("/api/inventario/**", "/api/compras/**",
                                "/api/reportes/**", "/api/proveedores/**", "/api/categorias/**",
                                "/api/admin/**", "/api/usuarios/**", "/api/gastos/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/productos/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(new SuscripcionFilter(configuracionSistemaService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // En produccion reemplaza "*" por el dominio real de tu frontend
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
