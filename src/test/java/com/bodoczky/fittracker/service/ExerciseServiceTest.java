package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.ExerciseHistoryEntryResponse;
import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.ExerciseResponse;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.ExerciseLog;
import com.bodoczky.fittracker.model.TrainingCycle;
import com.bodoczky.fittracker.model.WorkoutDay;
import com.bodoczky.fittracker.model.WorkoutSession;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.repository.ExerciseLogRepository;
import com.bodoczky.fittracker.repository.ExerciseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    private Exercise sampleExercise() {
        return Exercise.builder()
                .id(1L)
                .name("Bench Press")
                .category(ExerciseCategory.BENCH_PRESS)
                .description("Flat barbell bench")
                .build();
    }

    private ExerciseRequest sampleRequest() {
        return ExerciseRequest.builder()
                .name("Bench Press")
                .category(ExerciseCategory.BENCH_PRESS)
                .description("Flat barbell bench")
                .build();
    }

    @Test
    void getAllExercises_returnsMappedList() {
        when(exerciseRepository.findAll()).thenReturn(List.of(sampleExercise()));

        List<ExerciseResponse> result = exerciseService.getAllExercises();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Bench Press");
        assertThat(result.get(0).getCategory()).isEqualTo(ExerciseCategory.BENCH_PRESS);
    }

    @Test
    void getExerciseById_returnsResponse_whenFound() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(sampleExercise()));

        ExerciseResponse result = exerciseService.getExerciseById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Bench Press");
    }

    @Test
    void getExerciseById_throws_whenMissing() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.getExerciseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise")
                .hasMessageContaining("99");
    }

    @Test
    void getExercisesByCategory_returnsMappedList() {
        when(exerciseRepository.findByCategory(ExerciseCategory.SQUAT))
                .thenReturn(List.of(Exercise.builder().id(2L).name("Back Squat").category(ExerciseCategory.SQUAT).build()));

        List<ExerciseResponse> result = exerciseService.getExercisesByCategory(ExerciseCategory.SQUAT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(ExerciseCategory.SQUAT);
    }

    @Test
    void createExercise_persistsAndReturns() {
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> {
            Exercise e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        ExerciseResponse result = exerciseService.createExercise(sampleRequest());

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Bench Press");
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void updateExercise_updatesAndReturns_whenFound() {
        Exercise existing = sampleExercise();
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.save(any(Exercise.class))).thenAnswer(inv -> inv.getArgument(0));

        ExerciseRequest req = ExerciseRequest.builder()
                .name("Incline Bench")
                .category(ExerciseCategory.HORIZONTAL_PRESS)
                .description("30 deg incline")
                .build();

        ExerciseResponse result = exerciseService.updateExercise(1L, req);

        assertThat(result.getName()).isEqualTo("Incline Bench");
        assertThat(result.getCategory()).isEqualTo(ExerciseCategory.HORIZONTAL_PRESS);
        assertThat(result.getDescription()).isEqualTo("30 deg incline");
    }

    @Test
    void updateExercise_throws_whenMissing() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exerciseService.updateExercise(99L, sampleRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void deleteExercise_removes_whenExists() {
        when(exerciseRepository.existsById(1L)).thenReturn(true);

        exerciseService.deleteExercise(1L);

        verify(exerciseRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteExercise_throws_whenMissing() {
        when(exerciseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> exerciseService.deleteExercise(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(exerciseRepository, never()).deleteById(any());
    }

    private ExerciseLog historyLog(Exercise ex, LocalDate date, int cycleNumber, int microcycle,
                                   String actualPerformance, BigDecimal actualRpe, String notes) {
        TrainingCycle cycle = TrainingCycle.builder()
                .id(7L)
                .cycleNumber(cycleNumber)
                .numberOfMicrocycles(6)
                .startDate(date)
                .build();
        WorkoutDay day = WorkoutDay.builder().id(70L).trainingCycle(cycle).dayNumber(1).build();
        WorkoutSession session = WorkoutSession.builder()
                .id(700L)
                .workoutDay(day)
                .microcycleNumber(microcycle)
                .date(date)
                .build();
        return ExerciseLog.builder()
                .id(7000L)
                .workoutSession(session)
                .exercise(ex)
                .planned(false)
                .orderIndex(1)
                .actualPerformance(actualPerformance)
                .actualRpe(actualRpe)
                .notes(notes)
                .build();
    }

    @Test
    void getExerciseHistory_mapsLogToFlattenedEntry() {
        when(exerciseRepository.existsById(1L)).thenReturn(true);
        when(exerciseLogRepository.findHistory(1L, null, null)).thenReturn(List.of(
                historyLog(sampleExercise(), LocalDate.of(2026, 2, 10), 1, 2,
                        "100kg x5", new BigDecimal("8.5"), "top set")));

        List<ExerciseHistoryEntryResponse> result = exerciseService.getExerciseHistory(1L, null, null);

        assertThat(result).hasSize(1);
        ExerciseHistoryEntryResponse entry = result.get(0);
        assertThat(entry.getSessionDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        assertThat(entry.getTrainingCycleId()).isEqualTo(7L);
        assertThat(entry.getCycleNumber()).isEqualTo(1);
        assertThat(entry.getMicrocycleNumber()).isEqualTo(2);
        assertThat(entry.getActualPerformance()).isEqualTo("100kg x5");
        assertThat(entry.getActualRpe()).isEqualByComparingTo("8.5");
        assertThat(entry.getNotes()).isEqualTo("top set");
    }

    @Test
    void getExerciseHistory_throws_whenExerciseMissing() {
        when(exerciseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> exerciseService.getExerciseHistory(99L, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise");

        verify(exerciseLogRepository, never()).findHistory(any(), any(), any());
    }

    @Test
    void getExerciseHistory_passesDateBounds_andReturnsEmpty_whenNoLogs() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        when(exerciseRepository.existsById(1L)).thenReturn(true);
        when(exerciseLogRepository.findHistory(1L, from, to)).thenReturn(List.of());

        List<ExerciseHistoryEntryResponse> result = exerciseService.getExerciseHistory(1L, from, to);

        assertThat(result).isEmpty();
        verify(exerciseLogRepository).findHistory(1L, from, to);
    }
}
