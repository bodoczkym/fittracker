package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.ExerciseLogRequest;
import com.bodoczky.fittracker.dto.ExerciseLogResponse;
import com.bodoczky.fittracker.dto.WorkoutSessionRequest;
import com.bodoczky.fittracker.dto.WorkoutSessionResponse;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.service.WorkoutSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkoutSessionController.class)
class WorkoutSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private WorkoutSessionService workoutSessionService;

    private WorkoutSessionResponse sessionResponse;
    private ExerciseLogResponse logResponse;

    @BeforeEach
    void setUp() {
        sessionResponse = WorkoutSessionResponse.builder()
                .id(50L)
                .workoutDayId(10L)
                .weekNumber(1)
                .date(LocalDate.of(2026, 1, 5))
                .build();
        logResponse = ExerciseLogResponse.builder()
                .id(900L)
                .planned(false)
                .orderIndex(1)
                .build();
    }

    @Test
    void getSessions_byCycleOnly() throws Exception {
        when(workoutSessionService.getSessionsByCycle(1L)).thenReturn(List.of(sessionResponse));

        mockMvc.perform(get("/api/v1/sessions").param("cycleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50));
    }

    @Test
    void getSessions_byCycleAndWeek() throws Exception {
        when(workoutSessionService.getSessionsByCycleAndWeek(1L, 2)).thenReturn(List.of(sessionResponse));

        mockMvc.perform(get("/api/v1/sessions")
                        .param("cycleId", "1")
                        .param("weekNumber", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50));
    }

    @Test
    void getSessionById_returnsOne() throws Exception {
        when(workoutSessionService.getSessionById(50L)).thenReturn(sessionResponse);

        mockMvc.perform(get("/api/v1/sessions/50"))
                .andExpect(status().isOk());
    }

    @Test
    void getSessionById_returns404_whenMissing() throws Exception {
        when(workoutSessionService.getSessionById(99L))
                .thenThrow(new ResourceNotFoundException("WorkoutSession", 99L));

        mockMvc.perform(get("/api/v1/sessions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSession_returns201() throws Exception {
        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(10L)
                .weekNumber(1)
                .date(LocalDate.of(2026, 1, 5))
                .build();
        when(workoutSessionService.createSession(any(WorkoutSessionRequest.class))).thenReturn(sessionResponse);

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void createSession_returns400_whenInvalid() throws Exception {
        WorkoutSessionRequest req = WorkoutSessionRequest.builder().build();

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSession_returns200() throws Exception {
        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(10L)
                .weekNumber(1)
                .date(LocalDate.of(2026, 1, 5))
                .build();
        when(workoutSessionService.updateSession(eq(50L), any(WorkoutSessionRequest.class))).thenReturn(sessionResponse);

        mockMvc.perform(put("/api/v1/sessions/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteSession_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/sessions/50"))
                .andExpect(status().isNoContent());

        verify(workoutSessionService).deleteSession(50L);
    }

    @Test
    void addLog_returns201() throws Exception {
        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(5L)
                .planned(false)
                .orderIndex(1)
                .build();
        when(workoutSessionService.addLog(eq(50L), any(ExerciseLogRequest.class))).thenReturn(logResponse);

        mockMvc.perform(post("/api/v1/sessions/50/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(900));
    }
}
