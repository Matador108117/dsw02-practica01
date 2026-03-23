package com.dsw02.empleados.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "basicAuth";
        return new OpenAPI()
            .info(new Info()
                .title("API de Empleados, Departamentos y Auth")
                .version("4.0.0")
                .description("API de dominio en /api/v3 con HTTP Basic Auth y endpoints de autenticacion\n" +
                    "cookie-first en /api/v4/auth para login, refresh y logout con controles CSRF."))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("HTTP Basic Authentication using correo_electronico as username")));
    }
}
