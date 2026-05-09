package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.ExerciseLogRequest;
import com.bodoczky.fittracker.dto.ExerciseLogResponse;
import com.bodoczky.fittracker.service.WorkoutSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseLogController.class)
class ExerciseLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkoutSessionService workoutSessionService;

    @Test
    void updateLog_returns200() throws Exception {
        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(5L)
                .planned(true)
                .orderIndex(1)
                .build();
        ExerciseLogResponse response = ExerciseLogResponse.builder()
                .id(500L)
                .planned(true)
                .orderIndex(1)
                .build();
        when(workoutSessionService.updateLog(eq(500L), any(ExerciseLogRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/logs/500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500));
    }

    @Test
    void updateLog_returns400_whenInvalid() throws Exception {
        ExerciseLogRequest req = ExerciseLogRequest.builder().build();

        mockMvc.perform(put("/api/v1/logs/500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLog_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/logs/500"))
                .andExpect(status().isNoContent());

        verify(workoutSessionService).deleteLog(500L);
    }
}
