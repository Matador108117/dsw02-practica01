package com.dsw02.empleados.contract;

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

import com.dsw02.empleados.integration.BasePostgresIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoCredentialContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldNotExposeContrasenaFieldsInResponses() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Credencial",
            "direccion", "Oculta",
            "telefono", "5591231234",
            "correoElectronico", "credencial.contract@example.com",
            "contrasena", "Password123!"
        ));

        MvcResult created = mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode json = objectMapper.readTree(created.getResponse().getContentAsString());
        assertThat(json.has("contrasena")).isFalse();
        assertThat(json.has("contrasenaHash")).isFalse();
        assertThat(json.has("contrasena_hash")).isFalse();
    }
}
