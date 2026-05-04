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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoUpdateDeleteIntegrationIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateAndDelete() throws Exception {
        String createPayload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Marta",
            "direccion", "Centro",
            "telefono", "5555555555",
            "correoElectronico", "marta.integration@example.com",
            "contrasena", "Password123!"
        ));

        MvcResult created = mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isCreated())
            .andReturn();

        String clave = objectMapper.readTree(created.getResponse().getContentAsString()).get("clave").asText();

        String updatePayload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Marta 2",
            "direccion", "Centro 2",
            "telefono", "5666666666"
        ));

        mockMvc.perform(put("/api/v3/empleados/{clave}", clave)
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v3/empleados/{clave}", clave)
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldReassignDepartamentoOnV3Update() throws Exception {
        MvcResult departamentoCreated = mockMvc.perform(post("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nombre", "Soporte"))))
            .andExpect(status().isCreated())
            .andReturn();

        String departamentoId = objectMapper.readTree(departamentoCreated.getResponse().getContentAsString()).get("id").asText();

        String createPayload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Reasignable",
            "direccion", "Centro",
            "telefono", "5577777777",
            "correoElectronico", "reasignable@example.com",
            "contrasena", "Password123!"
        ));

        MvcResult created = mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isCreated())
            .andReturn();

        String clave = objectMapper.readTree(created.getResponse().getContentAsString()).get("clave").asText();

        String updatePayload = objectMapper.writeValueAsString(Map.of("departamentoId", departamentoId));

        mockMvc.perform(put("/api/v3/empleados/{clave}", clave)
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk());
    }
}
