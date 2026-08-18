package com.tienda.inventario.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Sin esto, Jackson no sabe serializar los proxies "perezosos" que genera
     * Hibernate para las relaciones @ManyToOne/@OneToMany (ej: Producto.categoria)
     * y revienta con "Type definition error: ByteBuddyInterceptor" en cuanto
     * esa relacion deja de ser null.
     */
    @Bean
    public Hibernate6Module hibernate6Module() {
        return new Hibernate6Module();
    }
}
