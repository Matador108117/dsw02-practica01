package com.dsw02.empleados.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldIssueSecureCookiesOnLoginAndAllowRefreshAndLogout() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v4/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "admin@empresa.com",
                    "password", "Admin123!"
                ))))
            .andExpect(status().isOk())
            .andReturn();

        List<String> setCookies = login.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).anyMatch(v -> v.startsWith("ACCESS_TOKEN=") && v.contains("HttpOnly") && v.contains("SameSite=Lax"));
        assertThat(setCookies).anyMatch(v -> v.startsWith("REFRESH_TOKEN=") && v.contains("HttpOnly") && v.contains("SameSite=Strict"));
        assertThat(setCookies).anyMatch(v -> v.startsWith("XSRF-TOKEN=") && !v.contains("HttpOnly"));

        String refreshValue = extractCookieValue(setCookies, "REFRESH_TOKEN");
        String csrfValue = extractCookieValue(setCookies, "XSRF-TOKEN");

        mockMvc.perform(post("/api/v4/auth/refresh")
                .cookie(new MockCookie("REFRESH_TOKEN", refreshValue), new MockCookie("XSRF-TOKEN", csrfValue))
                .header("X-CSRF-Token", csrfValue))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v4/auth/logout")
                .cookie(new MockCookie("REFRESH_TOKEN", refreshValue), new MockCookie("XSRF-TOKEN", csrfValue))
                .header("X-CSRF-Token", csrfValue))
            .andExpect(status().isNoContent());
    }

    private String extractCookieValue(List<String> setCookies, String name) {
        return setCookies.stream()
            .filter(v -> v.startsWith(name + "="))
            .findFirst()
            .map(v -> v.substring(name.length() + 1, v.indexOf(';')))
            .orElseThrow();
    }
}
