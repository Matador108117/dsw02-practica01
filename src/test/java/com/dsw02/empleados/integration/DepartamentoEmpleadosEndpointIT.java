package com.dsw02.empleados.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DepartamentoEmpleadosEndpointIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRespondFromDepartamentoEmpleadosEndpointWithAuth() throws Exception {
        mockMvc.perform(get("/api/v3/departamentos/DEP-1/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isOk());
    }
}
