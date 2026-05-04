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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dsw02.empleados.integration.BasePostgresIT;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DepartamentoCrudContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateReadAndUpdateDepartamento() throws Exception {
        String createPayload = objectMapper.writeValueAsString(Map.of("nombre", "Finanzas"));

        MvcResult created = mockMvc.perform(post("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload))
            .andExpect(status().isCreated())
            .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v3/departamentos/{id}", id)
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isOk());

        String updatePayload = objectMapper.writeValueAsString(Map.of("nombre", "Finanzas Corp"));
        mockMvc.perform(put("/api/v3/departamentos/{id}", id)
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk());
    }

    @Test
    void shouldApplyDefaultPaginationValues() throws Exception {
        mockMvc.perform(get("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(25));
    }

    @Test
    void shouldClampPaginationSizeToMax100() throws Exception {
        mockMvc.perform(get("/api/v3/departamentos?page=0&size=999")
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(100));
    }
}
