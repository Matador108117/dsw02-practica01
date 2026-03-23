package com.dsw02.empleados.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthCsrfCrudIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectRefreshWithoutCsrfHeader() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v4/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "admin@empresa.com",
                    "password", "Admin123!"
                ))))
            .andExpect(status().isOk())
            .andReturn();

        String refresh = extractCookieValue(login.getResponse().getHeaders("Set-Cookie"), "REFRESH_TOKEN");
        String csrf = extractCookieValue(login.getResponse().getHeaders("Set-Cookie"), "XSRF-TOKEN");

        mockMvc.perform(post("/api/v4/auth/refresh")
                .cookie(new MockCookie("REFRESH_TOKEN", refresh), new MockCookie("XSRF-TOKEN", csrf)))
            .andExpect(status().isForbidden());
    }

    private String extractCookieValue(java.util.List<String> setCookies, String name) {
        return setCookies.stream()
            .filter(v -> v.startsWith(name + "="))
            .findFirst()
            .map(v -> v.substring(name.length() + 1, v.indexOf(';')))
            .orElseThrow();
    }
}
