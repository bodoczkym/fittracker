package com.bodoczky.fittracker.integration;

import com.bodoczky.fittracker.dto.TrainingCycleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of the active-cycle endpoint and the one-active invariant
 * (ADR-0004), exercised against the real H2-backed persistence layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActiveCycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void getActiveCycle_returns404_whenNoCycles() throws Exception {
        mockMvc.perform(get("/api/v1/cycles/active").with(httpBasic("admin", "admin")))
                .andExpect(status().isNotFound());
    }

    @Test
    void creatingSecondCycle_endsFirst_andSecondBecomesActive() throws Exception {
        long idA = createCycle(1, LocalDate.of(2026, 1, 1));
        long idB = createCycle(2, LocalDate.of(2026, 3, 1));

        // The newest cycle is the active one.
        mockMvc.perform(get("/api/v1/cycles/active").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idB))
                .andExpect(jsonPath("$.endDate").doesNotExist());

        // The first cycle was auto-ended with today's date.
        mockMvc.perform(get("/api/v1/cycles/" + idA).with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endDate").value(LocalDate.now().toString()));
    }

    private long createCycle(int cycleNumber, LocalDate startDate) throws Exception {
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(cycleNumber)
                .numberOfMicrocycles(6)
                .startDate(startDate)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/cycles")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
