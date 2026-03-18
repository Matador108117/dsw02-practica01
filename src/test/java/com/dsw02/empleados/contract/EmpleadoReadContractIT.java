package com.dsw02.empleados.contract;

import com.dsw02.empleados.integration.BasePostgresIT;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmpleadoReadContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetListAndDetail() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
            "nombre", "Ana",
            "direccion", "Centro",
            "telefono", "5511111111"
        ));

        MvcResult created = mockMvc.perform(post("/api/v2/empleados")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn();

        String clave = objectMapper.readTree(created.getResponse().getContentAsString()).get("clave").asText();

        mockMvc.perform(get("/api/v2/empleados").with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v2/empleados/{clave}", clave).with(httpBasic("admin", "admin123")))
            .andExpect(status().isOk());
    }
}
