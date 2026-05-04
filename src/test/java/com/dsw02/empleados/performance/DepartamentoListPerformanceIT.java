package com.dsw02.empleados.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dsw02.empleados.integration.BasePostgresIT;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled("Performance baseline test. Run manually when measuring environment P95.")
class DepartamentoListPerformanceIT extends BasePostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldMeetP95Under800msForDepartamentoList() throws Exception {
        for (int i = 0; i < 30; i++) {
            mockMvc.perform(post("/api/v3/departamentos")
                    .with(httpBasic("admin@empresa.com", "Admin123!"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("nombre", "Depto Perf " + i))))
                .andExpect(status().isCreated());
        }

        List<Long> timesMs = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            long start = System.nanoTime();
            mockMvc.perform(get("/api/v3/departamentos?page=0&size=25")
                    .with(httpBasic("admin@empresa.com", "Admin123!")))
                .andExpect(status().isOk());
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            timesMs.add(elapsedMs);
        }

        long p95 = calculateP95(timesMs);
        assertThat(p95).isLessThanOrEqualTo(800L);
    }

    private long calculateP95(List<Long> samples) {
        List<Long> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int index = Math.max(0, (int) Math.ceil(sorted.size() * 0.95) - 1);
        return sorted.get(index);
    }
}
