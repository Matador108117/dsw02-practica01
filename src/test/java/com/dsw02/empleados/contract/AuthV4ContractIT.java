package com.dsw02.empleados.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.dsw02.empleados.integration.BasePostgresIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthV4ContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCookieFirstLoginSchemaForSuccess() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "email", "admin@empresa.com",
            "password", "Admin123!"
        ));

        MvcResult result = mockMvc.perform(post("/api/v4/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(json.get("role").asText()).isIn("ADMIN", "USER");
        assertThat(json.has("token")).isFalse();
        assertThat(json.has("refreshToken")).isFalse();
    }

    @Test
    void shouldReturnUnauthorizedWithoutTokenFieldsOnInvalidLogin() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "email", "admin@empresa.com",
            "password", "wrong-password"
        ));

        MvcResult result = mockMvc.perform(post("/api/v4/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isUnauthorized())
            .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("code").asText()).isEqualTo("AUTH_INVALID_CREDENTIALS");
        assertThat(json.has("token")).isFalse();
        assertThat(json.has("refreshToken")).isFalse();
    }
}
