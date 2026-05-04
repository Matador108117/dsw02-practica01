package com.dsw02.empleados.integration;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityCrudIntegrationIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnUnauthorizedWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/v3/empleados"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnOkWithValidCredentials() throws Exception {
        mockMvc.perform(get("/api/v3/empleados").with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminWriteOnV3() throws Exception {
        mockMvc.perform(post("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nombre", "Admin Allowed"))))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldAllowUserReadButForbidUserWriteOnV3() throws Exception {
        String userEmail = "readonly.user@example.com";
        String userPassword = "UserPassword123!";

        mockMvc.perform(post("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "nombre", "Readonly User",
                    "direccion", "Zona 1",
                    "telefono", "5500101010",
                    "correoElectronico", userEmail,
                    "contrasena", userPassword,
                    "rol", "USER"
                ))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v3/empleados")
                .with(httpBasic(userEmail, userPassword)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v3/departamentos")
                .with(httpBasic(userEmail, userPassword))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nombre", "Should Fail"))))
            .andExpect(status().isForbidden());
    }
}
