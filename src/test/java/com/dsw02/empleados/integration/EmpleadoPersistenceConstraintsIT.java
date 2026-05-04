package com.dsw02.empleados.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.repository.EmpleadoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoPersistenceConstraintsIT extends BasePostgresIT {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Test
    void shouldPersistCorreoElectronicoAndContrasenaHashForBootstrapAdmin() {
        Empleado admin = empleadoRepository.findByCorreoElectronicoIgnoreCase("admin@empresa.com").orElseThrow();
        assertThat(admin.getCorreoElectronico()).isNotBlank();
        assertThat(admin.getContrasenaHash()).isNotBlank();
        assertThat(admin.getContrasenaHash()).doesNotContain("Admin123!");
    }
}
