package com.upc.smaf.config;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate5JakartaModule hibernate5JakartaModule() {
        // Esto le enseña a Jackson cómo manejar los "proxies" de Hibernate
        return new Hibernate5JakartaModule();
    }
}