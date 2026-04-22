package com.dsw02.empleados.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dsw02.empleados.integration.BasePostgresIT;

@SpringBootTest(properties = {
    "app.api-version.sunset-v1-utc=2020-01-01T00:00:00Z",
    "app.api-version.release-v2-utc=2019-01-01T00:00:00Z"
})
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
