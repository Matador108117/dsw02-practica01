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
                .title("API CRUD de Empleados y Departamentos v3")
                .version("3.0.0")
                .description("API unificada en v3 con HTTP Basic Auth (correo_electronico:contrasena), \n" +
                    "autorizacion por rol (ADMIN=CRUD, USER=Read-only), bloqueo por intentos fallidos, \n" +
                    "y soporte de empleados/departamentos bajo /api/v3."))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("HTTP Basic Authentication using correo_electronico as username")));
    }
}
