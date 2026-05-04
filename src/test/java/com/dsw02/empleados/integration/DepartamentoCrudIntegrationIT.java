package com.dsw02.empleados.integration;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DepartamentoCrudIntegrationIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn409WhenDeletingDepartamentoWithEmpleados() throws Exception {
        MvcResult departamentoCreated = mockMvc.perform(post("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nombre", "Operaciones"))))
            .andExpect(status().isCreated())
            .andReturn();

        String departamentoId = objectMapper.readTree(departamentoCreated.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "nombre", "Empleado Conflicto",
                    "direccion", "Dir 1",
                    "telefono", "5512000000",
                    "correoElectronico", "conflicto@example.com",
                    "contrasena", "Password123!",
                    "departamentoId", departamentoId
                ))))
            .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v3/departamentos/{id}", departamentoId)
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isConflict());
    }
}
