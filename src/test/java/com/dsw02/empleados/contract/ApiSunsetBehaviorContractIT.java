package com.dsw02.empleados.contract;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.dsw02.empleados.integration.BasePostgresIT;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSunsetBehaviorContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldEnforceVersionPolicyRoute() throws Exception {
        mockMvc.perform(get("/api/v1/empleados")
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isGone());
    }
}
