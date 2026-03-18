package com.dsw02.empleados.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityCrudIntegrationIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnauthorizedWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/v2/empleados"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnOkWithValidCredentials() throws Exception {
        mockMvc.perform(get("/api/v2/empleados").with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk());
    }
}
