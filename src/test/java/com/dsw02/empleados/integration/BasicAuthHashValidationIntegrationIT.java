package com.dsw02.empleados.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BasicAuthHashValidationIntegrationIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectInvalidPasswordAgainstStoredHash() throws Exception {
        mockMvc.perform(get("/api/v3/empleados")
                .with(httpBasic("admin@empresa.com", "WrongPassword123!")))
            .andExpect(status().isUnauthorized());
    }
}
