package com.dsw02.empleados.contract;

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

import com.dsw02.empleados.integration.BasePostgresIT;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoUpdateDeleteContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateAndDeleteEmpleado() throws Exception {
        String createPayload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Ana",
            "direccion", "Centro",
            "telefono", "5511111111",
            "correoElectronico", "ana@example.com",
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
            "nombre", "Ana Mod",
            "direccion", "Centro 2",
            "telefono", "5522222222"
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
    void shouldReturn422OnV3UpdateWithMissingDepartamentoReference() throws Exception {
        String createPayload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Ana V3",
            "direccion", "Centro V3",
            "telefono", "5500112233",
            "correoElectronico", "ana.v3@example.com",
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
            "departamentoId", "DEP-999999"
        ));

        mockMvc.perform(put("/api/v3/empleados/{clave}", clave)
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldForbidUserRoleOnV3EmpleadoUpdate() throws Exception {
        String userEmail = "contract.readonly.update@example.com";
        String userPassword = "UserPassword123!";

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "nombre", "Readonly Updater",
                    "direccion", "Zona 2",
                    "telefono", "5510202020",
                    "correoElectronico", userEmail,
                    "contrasena", userPassword,
                    "rol", "USER"
                ))))
            .andExpect(status().isCreated());

        MvcResult targetCreated = mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "nombre", "Target",
                    "direccion", "Zona 3",
                    "telefono", "5510303030",
                    "correoElectronico", "target.update@example.com",
                    "contrasena", "Password123!"
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        String clave = objectMapper.readTree(targetCreated.getResponse().getContentAsString()).get("clave").asText();

        mockMvc.perform(put("/api/v3/empleados/{clave}", clave)
                .with(httpBasic(userEmail, userPassword))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nombre", "No permitido"))))
            .andExpect(status().isForbidden());
    }
}
