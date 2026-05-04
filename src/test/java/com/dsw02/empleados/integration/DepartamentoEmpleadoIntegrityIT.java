package com.dsw02.empleados.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.dsw02.empleados.model.Departamento;
import com.dsw02.empleados.model.Empleado;
import com.dsw02.empleados.repository.DepartamentoRepository;
import com.dsw02.empleados.repository.EmpleadoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class DepartamentoEmpleadoIntegrityIT extends BasePostgresIT {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Test
    void shouldAllowNullableDepartamentoAssignmentAndReferentialIntegrity() {
        Empleado admin = empleadoRepository.findByCorreoElectronicoIgnoreCase("admin@empresa.com").orElseThrow();
        assertThat(admin.getDepartamento()).isNull();

        Departamento dep = new Departamento();
        dep.setId("DEP-TST");
        dep.setNombre("Test Departamento");
        departamentoRepository.save(dep);

        admin.setDepartamento(dep);
        Empleado saved = empleadoRepository.save(admin);
        assertThat(saved.getDepartamento()).isNotNull();
        assertThat(saved.getDepartamento().getId()).isEqualTo("DEP-TST");
    }
}
