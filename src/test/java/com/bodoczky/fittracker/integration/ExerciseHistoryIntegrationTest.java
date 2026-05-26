package com.bodoczky.fittracker.integration;

import com.bodoczky.fittracker.dto.ExerciseLogRequest;
import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.TrainingCycleRequest;
import com.bodoczky.fittracker.dto.WorkoutDayRequest;
import com.bodoczky.fittracker.dto.WorkoutSessionRequest;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of issue #13: the per-exercise history endpoint, exercised against the real
 * H2-backed persistence layer through the full cycle → session → log chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExerciseHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void history_returnsFlattenedEntries_mostRecentFirst() throws Exception {
        long exerciseId = createExercise("Back Squat");
        JsonNode cycle = createCycleWithOneDay(1, LocalDate.of(2026, 1, 1));
        long cycleId = cycle.get("id").asLong();
        long dayId = cycle.get("workoutDays").get(0).get("id").asLong();

        logPerformance(dayId, exerciseId, 1, LocalDate.of(2026, 1, 5), "80kg x5", "8.0", "opener");
        logPerformance(dayId, exerciseId, 3, LocalDate.of(2026, 2, 10), "100kg x5", "8.5", "peak");

        mockMvc.perform(get("/api/v1/exercises/" + exerciseId + "/history").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                // Most recent first.
                .andExpect(jsonPath("$[0].sessionDate").value("2026-02-10"))
                .andExpect(jsonPath("$[0].trainingCycleId").value((int) cycleId))
                .andExpect(jsonPath("$[0].cycleNumber").value(1))
                .andExpect(jsonPath("$[0].microcycleNumber").value(3))
                .andExpect(jsonPath("$[0].actualPerformance").value("100kg x5"))
                .andExpect(jsonPath("$[0].actualRpe").value(8.5))
                .andExpect(jsonPath("$[0].notes").value("peak"))
                .andExpect(jsonPath("$[1].sessionDate").value("2026-01-05"))
                .andExpect(jsonPath("$[1].microcycleNumber").value(1));
    }

    @Test
    void history_filtersByDateRange() throws Exception {
        long exerciseId = createExercise("Back Squat");
        long dayId = createCycleWithOneDay(1, LocalDate.of(2026, 1, 1))
                .get("workoutDays").get(0).get("id").asLong();

        logPerformance(dayId, exerciseId, 1, LocalDate.of(2026, 1, 5), "80kg x5", "8.0", "opener");
        logPerformance(dayId, exerciseId, 3, LocalDate.of(2026, 2, 10), "100kg x5", "8.5", "peak");

        // Closed range catches only the February session.
        mockMvc.perform(get("/api/v1/exercises/" + exerciseId + "/history")
                        .with(httpBasic("admin", "admin"))
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sessionDate").value("2026-02-10"));

        // Half-open lower bound past both sessions yields nothing.
        mockMvc.perform(get("/api/v1/exercises/" + exerciseId + "/history")
                        .with(httpBasic("admin", "admin"))
                        .param("from", "2026-03-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void history_returnsEmptyList_whenExerciseHasNoLogs() throws Exception {
        long exerciseId = createExercise("Unused Movement");

        mockMvc.perform(get("/api/v1/exercises/" + exerciseId + "/history").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void history_returns404_whenExerciseMissing() throws Exception {
        mockMvc.perform(get("/api/v1/exercises/999999/history").with(httpBasic("admin", "admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private long createExercise(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/exercises")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ExerciseRequest.builder()
                                .name(name)
                                .category(ExerciseCategory.SQUAT)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private JsonNode createCycleWithOneDay(int cycleNumber, LocalDate startDate) throws Exception {
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(cycleNumber)
                .numberOfMicrocycles(6)
                .startDate(startDate)
                .workoutDays(List.of(WorkoutDayRequest.builder().dayNumber(1).name("Lower").build()))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/cycles")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /** Creates a session on the given day and logs an actual performance for the exercise. */
    private void logPerformance(long dayId, long exerciseId, int microcycle, LocalDate date,
                                String performance, String rpe, String notes) throws Exception {
        MvcResult sessionResult = mockMvc.perform(post("/api/v1/sessions")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkoutSessionRequest.builder()
                                .workoutDayId(dayId)
                                .microcycleNumber(microcycle)
                                .date(date)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        long sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/logs")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ExerciseLogRequest.builder()
                                .exerciseId(exerciseId)
                                .planned(false)
                                .orderIndex(1)
                                .actualPerformance(performance)
                                .actualRpe(new BigDecimal(rpe))
                                .notes(notes)
                                .build())))
                .andExpect(status().isCreated());
    }
}
