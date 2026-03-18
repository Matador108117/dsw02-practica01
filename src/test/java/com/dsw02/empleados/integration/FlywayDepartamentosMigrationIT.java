package com.dsw02.empleados.integration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FlywayDepartamentosMigrationIT extends BasePostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplyV10AndV11MigrationsSuccessfully() {
        Integer appliedCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version IN ('10', '11')
              AND success = TRUE
            """,
            Integer.class
        );

        assertThat(appliedCount).isEqualTo(2);
    }
}
