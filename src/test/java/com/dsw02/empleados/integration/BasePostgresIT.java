package com.dsw02.empleados.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class BasePostgresIT {

    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("empleados_test")
        .withUsername("empleados")
        .withPassword("empleados");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.bootstrap-admin.email", () -> "admin@empresa.com");
        registry.add("app.bootstrap-admin.password", () -> "Admin123!");
        registry.add("app.bootstrap-admin.only-once", () -> "false");
    }
}
