package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.config.SecurityConfig;
import com.bodoczky.fittracker.dto.ExportResponse;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebMvcTest(ExportController.class)
@Import(SecurityConfig.class)
@WithMockUser
class ExportControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void export_returns200_withSerialisedTree() throws Exception {
        ExportResponse response = ExportResponse.builder()
                .exportedAt(Instant.parse("2026-05-26T10:00:00Z"))
                .exercises(List.of(ExportResponse.Exercise.builder()
                        .id(5L).name("Bench Press").category(ExerciseCategory.BENCH_PRESS).build()))
                .trainingCycles(List.of(ExportResponse.TrainingCycle.builder()
                        .id(1L).cycleNumber(1).build()))
                .build();
        when(exportService.exportAll()).thenReturn(response);

        mockMvc.perform(get("/api/v1/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exportedAt").value("2026-05-26T10:00:00Z"))
                .andExpect(jsonPath("$.exercises[0].id").value(5))
                .andExpect(jsonPath("$.trainingCycles[0].cycleNumber").value(1));
    }
}
