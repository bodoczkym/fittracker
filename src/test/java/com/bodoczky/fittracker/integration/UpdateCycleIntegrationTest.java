package com.bodoczky.fittracker.integration;

import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.PlannedExerciseRequest;
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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of issue #12: a full PUT on a cycle replaces its nested workout days,
 * but must leave already-created {@link com.bodoczky.fittracker.model.WorkoutSession} rows and
 * their snapshot logs frozen (ADR-0003). Exercised against the real H2-backed persistence layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UpdateCycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void puttingCycleWithFewerDays_dropsRemovedDay_butKeepsSessionsOnRetainedDays() throws Exception {
        long exerciseId = createExercise();

        // Cycle with three days, each carrying one planned exercise.
        JsonNode cycle = createCycle(TrainingCycleRequest.builder()
                .cycleNumber(1)
                .numberOfMicrocycles(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .workoutDays(List.of(day(1, exerciseId), day(2, exerciseId), day(3, exerciseId)))
                .build());
        long cycleId = cycle.get("id").asLong();
        long day2Id = dayIdByNumber(cycle, 2);

        // A logged session against day 2 — its logs are snapshots of day 2's plan.
        JsonNode session = createSession(day2Id);
        long sessionId = session.get("id").asLong();
        long logId = session.get("exerciseLogs").get(0).get("id").asLong();

        // Full PUT keeping only days 1 and 2 (drop day 3).
        mockMvc.perform(put("/api/v1/cycles/" + cycleId)
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TrainingCycleRequest.builder()
                                .cycleNumber(1)
                                .numberOfMicrocycles(6)
                                .startDate(LocalDate.of(2026, 1, 1))
                                .workoutDays(List.of(day(1, exerciseId), day(2, exerciseId)))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workoutDays.length()").value(2));

        // Day 2 was kept in place, so the session and its frozen log survive untouched.
        mockMvc.perform(get("/api/v1/sessions/" + sessionId).with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId))
                .andExpect(jsonPath("$.workoutDayId").value(day2Id))
                .andExpect(jsonPath("$.exerciseLogs.length()").value(1))
                .andExpect(jsonPath("$.exerciseLogs[0].id").value(logId));
    }

    private long createExercise() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/exercises")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ExerciseRequest.builder()
                                .name("Bench Press")
                                .category(ExerciseCategory.BENCH_PRESS)
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private WorkoutDayRequest day(int dayNumber, long exerciseId) {
        return WorkoutDayRequest.builder()
                .dayNumber(dayNumber)
                .name("Day " + dayNumber)
                .plannedExercises(List.of(PlannedExerciseRequest.builder()
                        .exerciseId(exerciseId)
                        .orderIndex(1)
                        .sets(3)
                        .repRange("8-10")
                        .restPeriod("90s")
                        .build()))
                .build();
    }

    private JsonNode createCycle(TrainingCycleRequest req) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/cycles")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode createSession(long workoutDayId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/sessions")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkoutSessionRequest.builder()
                                .workoutDayId(workoutDayId)
                                .microcycleNumber(1)
                                .date(LocalDate.of(2026, 1, 3))
                                .build())))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private long dayIdByNumber(JsonNode cycle, int dayNumber) {
        for (JsonNode day : cycle.get("workoutDays")) {
            if (day.get("dayNumber").asInt() == dayNumber) {
                return day.get("id").asLong();
            }
        }
        throw new AssertionError("No workout day with dayNumber " + dayNumber);
    }
}
