package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.CopyCycleRequest;
import com.bodoczky.fittracker.dto.TrainingCycleRequest;
import com.bodoczky.fittracker.dto.TrainingCycleResponse;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.service.TrainingCycleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingCycleController.class)
class TrainingCycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingCycleService trainingCycleService;

    private TrainingCycleResponse sample;

    @BeforeEach
    void setUp() {
        sample = TrainingCycleResponse.builder()
                .id(1L)
                .cycleNumber(1)
                .numberOfWeeks(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    @Test
    void getAllCycles_returnsList() throws Exception {
        when(trainingCycleService.getAllCycles()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/cycles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getCycleById_returnsOne() throws Exception {
        when(trainingCycleService.getCycleById(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/v1/cycles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycleNumber").value(1));
    }

    @Test
    void getCycleById_returns404_whenMissing() throws Exception {
        when(trainingCycleService.getCycleById(99L))
                .thenThrow(new ResourceNotFoundException("TrainingCycle", 99L));

        mockMvc.perform(get("/api/v1/cycles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCycle_returns201() throws Exception {
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(1)
                .numberOfWeeks(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
        when(trainingCycleService.createCycle(any(TrainingCycleRequest.class))).thenReturn(sample);

        mockMvc.perform(post("/api/v1/cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createCycle_returns400_whenInvalid() throws Exception {
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(null)
                .startDate(null)
                .build();

        mockMvc.perform(post("/api/v1/cycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void copyFromPreviousCycle_returns201() throws Exception {
        CopyCycleRequest req = CopyCycleRequest.builder()
                .startDate(LocalDate.of(2026, 3, 1))
                .notes("next block")
                .build();
        when(trainingCycleService.copyFromPreviousCycle(eq(LocalDate.of(2026, 3, 1)), eq("next block")))
                .thenReturn(sample);

        mockMvc.perform(post("/api/v1/cycles/copy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateCycle_returns200() throws Exception {
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(1)
                .numberOfWeeks(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
        when(trainingCycleService.updateCycle(eq(1L), any(TrainingCycleRequest.class))).thenReturn(sample);

        mockMvc.perform(put("/api/v1/cycles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void endCycle_returns200() throws Exception {
        when(trainingCycleService.endCycle(1L)).thenReturn(sample);

        mockMvc.perform(patch("/api/v1/cycles/1/end"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCycle_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/cycles/1"))
                .andExpect(status().isNoContent());

        verify(trainingCycleService).deleteCycle(1L);
    }
}
