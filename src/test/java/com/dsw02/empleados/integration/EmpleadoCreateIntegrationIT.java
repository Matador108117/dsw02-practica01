package com.dsw02.empleados.integration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dsw02.empleados.repository.EmpleadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoCreateIntegrationIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Test
    void shouldCreateEmpleadoWithAuth() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Carlos",
            "direccion", "Norte",
            "telefono", "5533333333",
            "correoElectronico", "carlos.integration@example.com",
            "contrasena", "Password123!"
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateEmpleadoOnV3WithDepartamento() throws Exception {
        MvcResult departamentoCreated = mockMvc.perform(post("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nombre", "RRHH"))))
            .andExpect(status().isCreated())
            .andReturn();

        String departamentoId = objectMapper.readTree(departamentoCreated.getResponse().getContentAsString()).get("id").asText();

        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Carlos V3",
            "direccion", "Norte V3",
            "telefono", "5533000000",
            "correoElectronico", "carlos.v3@example.com",
            "contrasena", "Password123!",
            "departamentoId", departamentoId
        ));

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldPersistOnlyPasswordHashAndNeverPlaintext() throws Exception {
        String rawPassword = "Plaintext123!";
        String email = "credencial.integration@example.com";
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Hash Only",
            "direccion", "Segura",
            "telefono", "5533999999",
            "correoElectronico", email,
            "contrasena", rawPassword
        ));

        MvcResult created = mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = created.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("contrasena");
        assertThat(responseBody).doesNotContain(rawPassword);

        var empleado = empleadoRepository.findByCorreoElectronicoIgnoreCase(email).orElseThrow();
        assertThat(empleado.getContrasenaHash()).isNotBlank();
        assertThat(empleado.getContrasenaHash()).isNotEqualTo(rawPassword);
    }
}
