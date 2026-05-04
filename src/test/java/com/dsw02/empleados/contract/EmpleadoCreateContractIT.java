package com.dsw02.empleados.contract;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dsw02.empleados.integration.BasePostgresIT;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoCreateContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateEmpleado() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Juan Perez",
            "direccion", "Calle 1",
            "telefono", "5512345678",
            "correoElectronico", "juan.perez@example.com",
            "contrasena", "Password123!"
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clave").value(org.hamcrest.Matchers.matchesPattern("^EMP-\\d{6,}$")));
    }

    @Test
    void shouldRejectUnknownClaveOnCreate() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Juan Perez",
            "direccion", "Calle 1",
            "telefono", "5512345678",
            "correoElectronico", "juan2.perez@example.com",
            "contrasena", "Password123!",
            "clave", "EMP-999999"
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowNullDepartamentoIdOnV3Create() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Maria",
            "direccion", "Av 2",
            "telefono", "5599999999",
            "correoElectronico", "maria.null.depto@example.com",
            "contrasena", "Password123!"
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn422OnV3CreateWithMissingDepartamentoReference() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Maria",
            "direccion", "Av 3",
            "telefono", "5588888888",
            "correoElectronico", "maria.invalid.depto@example.com",
            "contrasena", "Password123!",
            "departamentoId", "DEP-999999"
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldForbidUserRoleOnV3EmpleadoCreate() throws Exception {
        String userEmail = "contract.readonly@example.com";
        String userPassword = "UserPassword123!";

        String bootstrapUserPayload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Contract Readonly",
            "direccion", "Zona 9",
            "telefono", "5510101010",
            "correoElectronico", userEmail,
            "contrasena", userPassword,
            "rol", "USER"
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(bootstrapUserPayload))
            .andExpect(status().isCreated());

        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "No Permitido",
            "direccion", "Zona 9",
            "telefono", "5510101011",
            "correoElectronico", "no.permitido@example.com",
            "contrasena", "Password123!"
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic(userEmail, userPassword))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isForbidden());
    }
}
