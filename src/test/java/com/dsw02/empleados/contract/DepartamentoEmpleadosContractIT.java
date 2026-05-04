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
class DepartamentoEmpleadosContractIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListEmpleadosByDepartamento() throws Exception {
        MvcResult departamentoCreated = mockMvc.perform(post("/api/v3/departamentos")
                .with(httpBasic("admin@empresa.com", "Admin123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("nombre", "Ventas"))))
            .andExpect(status().isCreated())
            .andReturn();

        String departamentoId = objectMapper.readTree(departamentoCreated.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v3/departamentos/{id}/empleados", departamentoId)
                .with(httpBasic("admin@empresa.com", "Admin123!")))
            .andExpect(status().isOk());
    }
}
