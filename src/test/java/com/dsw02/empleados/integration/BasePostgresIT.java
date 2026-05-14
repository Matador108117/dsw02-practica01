package com.dsw02.empleados.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class BasePostgresIT {

    private static final String externalDbUrl = System.getenv("TEST_DB_URL");
    private static final String externalDbUser = System.getenv("TEST_DB_USER");
    private static final String externalDbPassword = System.getenv("TEST_DB_PASSWORD");
    private static final String dockerApiVersionEnv = System.getenv("DOCKER_API_VERSION");
    private static final boolean useExternalDb = externalDbUrl != null && !externalDbUrl.isBlank();

    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("empleados_test")
        .withUsername("empleados")
        .withPassword("empleados");

    static {
        if (!useExternalDb) {
            if (System.getProperty("api.version") == null
                && (dockerApiVersionEnv == null || dockerApiVersionEnv.isBlank())) {
                System.setProperty("api.version", "1.40");
            }
            postgres.start();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (useExternalDb) {
            registry.add("spring.datasource.url", () -> externalDbUrl);
            registry.add("spring.datasource.username", () -> externalDbUser != null ? externalDbUser : "empleados");
            registry.add("spring.datasource.password", () -> externalDbPassword != null ? externalDbPassword : "empleados");
        } else {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
        registry.add("app.bootstrap-admin.email", () -> "admin@empresa.com");
        registry.add("app.bootstrap-admin.password", () -> "Admin123!");
        registry.add("app.bootstrap-admin.only-once", () -> "false");
    }
}
