package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.ExerciseResponse;
import com.bodoczky.fittracker.config.SecurityConfig;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.service.ExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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

@WebMvcTest(ExerciseController.class)
@Import(SecurityConfig.class)
@WithMockUser
class ExerciseControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private ExerciseService exerciseService;

    private ExerciseResponse sample;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
        sample = ExerciseResponse.builder()
                .id(1L)
                .name("Bench Press")
                .category(ExerciseCategory.BENCH_PRESS)
                .description("flat barbell")
                .build();
    }

    @Test
    void getAllExercises_returnsList() throws Exception {
        when(exerciseService.getAllExercises()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Bench Press"));
    }

    @Test
    void getExerciseById_returnsOne() throws Exception {
        when(exerciseService.getExerciseById(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/v1/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getExerciseById_returns404_whenMissing() throws Exception {
        when(exerciseService.getExerciseById(99L))
                .thenThrow(new ResourceNotFoundException("Exercise", 99L));

        mockMvc.perform(get("/api/v1/exercises/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getExercisesByCategory_returnsList() throws Exception {
        when(exerciseService.getExercisesByCategory(ExerciseCategory.SQUAT)).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/exercises/category/SQUAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createExercise_returns201() throws Exception {
        ExerciseRequest req = ExerciseRequest.builder()
                .name("Bench Press")
                .category(ExerciseCategory.BENCH_PRESS)
                .description("flat barbell")
                .build();
        when(exerciseService.createExercise(any(ExerciseRequest.class))).thenReturn(sample);

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createExercise_returns400_whenInvalid() throws Exception {
        ExerciseRequest req = ExerciseRequest.builder()
                .name("")
                .category(null)
                .build();

        mockMvc.perform(post("/api/v1/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExercise_returns200() throws Exception {
        ExerciseRequest req = ExerciseRequest.builder()
                .name("Bench Press")
                .category(ExerciseCategory.BENCH_PRESS)
                .build();
        when(exerciseService.updateExercise(eq(1L), any(ExerciseRequest.class))).thenReturn(sample);

        mockMvc.perform(put("/api/v1/exercises/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteExercise_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/exercises/1"))
                .andExpect(status().isNoContent());

        verify(exerciseService).deleteExercise(1L);
    }
}
