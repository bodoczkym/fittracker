package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.ExportResponse;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.ExerciseLog;
import com.bodoczky.fittracker.model.PlannedExercise;
import com.bodoczky.fittracker.model.TrainingCycle;
import com.bodoczky.fittracker.model.WorkoutDay;
import com.bodoczky.fittracker.model.WorkoutSession;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.repository.ExerciseRepository;
import com.bodoczky.fittracker.repository.TrainingCycleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private TrainingCycleRepository trainingCycleRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExportService exportService;

    private Exercise exercise() {
        return Exercise.builder()
                .id(5L)
                .name("Bench Press")
                .category(ExerciseCategory.BENCH_PRESS)
                .description("flat barbell")
                .build();
    }

    /** A full one-of-each graph: cycle → day → (planned exercise, session → log). */
    private TrainingCycle fullCycle(Exercise ex) {
        TrainingCycle cycle = TrainingCycle.builder()
                .id(1L)
                .cycleNumber(1)
                .numberOfMicrocycles(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .notes("block A")
                .build();
        WorkoutDay day = WorkoutDay.builder().id(10L).trainingCycle(cycle).dayNumber(1).name("Push").build();
        day.getPlannedExercises().add(PlannedExercise.builder()
                .id(100L).workoutDay(day).exercise(ex).orderIndex(1)
                .sets(3).repRange("8-10").restPeriod("90s").targetRpe(new BigDecimal("8.0")).build());
        WorkoutSession session = WorkoutSession.builder()
                .id(1000L).workoutDay(day).microcycleNumber(2).date(LocalDate.of(2026, 1, 8)).build();
        session.getExerciseLogs().add(ExerciseLog.builder()
                .id(10000L).workoutSession(session).exercise(ex).planned(true).orderIndex(1)
                .actualPerformance("100kg x5").actualRpe(new BigDecimal("8.5")).notes("solid").build());
        day.getWorkoutSessions().add(session);
        cycle.getWorkoutDays().add(day);
        return cycle;
    }

    @Test
    void exportAll_buildsLosslessNestedTree() {
        Exercise ex = exercise();
        when(exerciseRepository.findAll()).thenReturn(List.of(ex));
        when(trainingCycleRepository.findAll()).thenReturn(List.of(fullCycle(ex)));

        ExportResponse export = exportService.exportAll();

        assertThat(export.getExportedAt()).isNotNull();
        assertThat(export.getExercises()).hasSize(1);
        assertThat(export.getExercises().get(0).getId()).isEqualTo(5L);
        assertThat(export.getExercises().get(0).getCategory()).isEqualTo(ExerciseCategory.BENCH_PRESS);

        assertThat(export.getTrainingCycles()).hasSize(1);
        ExportResponse.TrainingCycle cycle = export.getTrainingCycles().get(0);
        assertThat(cycle.getCycleNumber()).isEqualTo(1);
        assertThat(cycle.getWorkoutDays()).hasSize(1);

        ExportResponse.WorkoutDay day = cycle.getWorkoutDays().get(0);
        assertThat(day.getPlannedExercises()).hasSize(1);
        // Catalog reference is a bare id, not a re-nested exercise.
        assertThat(day.getPlannedExercises().get(0).getExerciseId()).isEqualTo(5L);
        assertThat(day.getWorkoutSessions()).hasSize(1);

        ExportResponse.WorkoutSession session = day.getWorkoutSessions().get(0);
        assertThat(session.getMicrocycleNumber()).isEqualTo(2);
        assertThat(session.getExerciseLogs()).hasSize(1);
        ExportResponse.ExerciseLog log = session.getExerciseLogs().get(0);
        assertThat(log.getExerciseId()).isEqualTo(5L);
        assertThat(log.getActualPerformance()).isEqualTo("100kg x5");
    }

    @Test
    void exportAll_returnsEmptyCollections_whenDatabaseEmpty() {
        when(exerciseRepository.findAll()).thenReturn(List.of());
        when(trainingCycleRepository.findAll()).thenReturn(List.of());

        ExportResponse export = exportService.exportAll();

        assertThat(export.getExportedAt()).isNotNull();
        assertThat(export.getExercises()).isEmpty();
        assertThat(export.getTrainingCycles()).isEmpty();
    }
}
