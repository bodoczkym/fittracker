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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of issue #16: the full-database JSON export. Verifies the auth boundary and
 * that a seeded cycle → day → planned/session → log graph round-trips into the export document,
 * exercised against the real H2-backed persistence layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void export_returns401_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/export"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void export_includesEveryRow_acrossCatalogAndCycleTree() throws Exception {
        long exerciseId = createExercise();
        JsonNode cycle = createCycleWithPlannedExercise(exerciseId);
        long cycleId = cycle.get("id").asLong();
        long dayId = cycle.get("workoutDays").get(0).get("id").asLong();
        createSession(dayId); // snapshots the planned exercise into one log

        // The seeding above shares this test's transaction and persistence context, where the
        // workout day's inverse session collection was never synced. Flush + clear so the export
        // reloads from the database the way a real, separate request would.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/v1/export").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exportedAt").isNotEmpty())
                // Catalog.
                .andExpect(jsonPath("$.exercises.length()").value(1))
                .andExpect(jsonPath("$.exercises[0].id").value((int) exerciseId))
                .andExpect(jsonPath("$.exercises[0].name").value("Bench Press"))
                // Cycle tree.
                .andExpect(jsonPath("$.trainingCycles.length()").value(1))
                .andExpect(jsonPath("$.trainingCycles[0].id").value((int) cycleId))
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays.length()").value(1))
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays[0].id").value((int) dayId))
                // Planned exercise references the catalog by id.
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays[0].plannedExercises.length()").value(1))
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays[0].plannedExercises[0].exerciseId")
                        .value((int) exerciseId))
                // Session and its snapshot log.
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays[0].workoutSessions.length()").value(1))
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays[0].workoutSessions[0].microcycleNumber")
                        .value(1))
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays[0].workoutSessions[0].exerciseLogs.length()")
                        .value(1))
                .andExpect(jsonPath("$.trainingCycles[0].workoutDays[0].workoutSessions[0].exerciseLogs[0].exerciseId")
                        .value((int) exerciseId));
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

    private JsonNode createCycleWithPlannedExercise(long exerciseId) throws Exception {
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(1)
                .numberOfMicrocycles(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .workoutDays(List.of(WorkoutDayRequest.builder()
                        .dayNumber(1)
                        .name("Push")
                        .plannedExercises(List.of(PlannedExerciseRequest.builder()
                                .exerciseId(exerciseId)
                                .orderIndex(1)
                                .sets(3)
                                .repRange("8-10")
                                .restPeriod("90s")
                                .build()))
                        .build()))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/cycles")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void createSession(long dayId) throws Exception {
        mockMvc.perform(post("/api/v1/sessions")
                        .with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkoutSessionRequest.builder()
                                .workoutDayId(dayId)
                                .microcycleNumber(1)
                                .date(LocalDate.of(2026, 1, 3))
                                .build())))
                .andExpect(status().isCreated());
    }
}
