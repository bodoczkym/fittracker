package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.PlannedExerciseRequest;
import com.bodoczky.fittracker.dto.TrainingCycleRequest;
import com.bodoczky.fittracker.dto.TrainingCycleResponse;
import com.bodoczky.fittracker.dto.WorkoutDayRequest;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.PlannedExercise;
import com.bodoczky.fittracker.model.TrainingCycle;
import com.bodoczky.fittracker.model.WorkoutDay;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingCycleServiceTest {

    @Mock
    private TrainingCycleRepository trainingCycleRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private TrainingCycleService trainingCycleService;

    private Exercise exercise(long id) {
        return Exercise.builder()
                .id(id)
                .name("Exercise " + id)
                .category(ExerciseCategory.BENCH_PRESS)
                .build();
    }

    private TrainingCycle cycleWithDayAndExercise() {
        Exercise ex = exercise(5L);
        TrainingCycle cycle = TrainingCycle.builder()
                .id(1L)
                .cycleNumber(1)
                .numberOfWeeks(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
        WorkoutDay day = WorkoutDay.builder()
                .id(10L)
                .trainingCycle(cycle)
                .dayNumber(1)
                .name("Push")
                .build();
        PlannedExercise pe = PlannedExercise.builder()
                .id(100L)
                .workoutDay(day)
                .exercise(ex)
                .orderIndex(1)
                .sets(3)
                .repRange("8-10")
                .restPeriod("90s")
                .targetRpe(new BigDecimal("8.0"))
                .build();
        day.getPlannedExercises().add(pe);
        cycle.getWorkoutDays().add(day);
        return cycle;
    }

    @Test
    void getAllCycles_returnsMappedList() {
        when(trainingCycleRepository.findAll()).thenReturn(List.of(cycleWithDayAndExercise()));

        List<TrainingCycleResponse> result = trainingCycleService.getAllCycles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCycleNumber()).isEqualTo(1);
        assertThat(result.get(0).getWorkoutDays()).hasSize(1);
        assertThat(result.get(0).getWorkoutDays().get(0).getPlannedExercises()).hasSize(1);
    }

    @Test
    void getCycleById_returns_whenFound() {
        when(trainingCycleRepository.findById(1L)).thenReturn(Optional.of(cycleWithDayAndExercise()));

        TrainingCycleResponse result = trainingCycleService.getCycleById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCycleById_throws_whenMissing() {
        when(trainingCycleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingCycleService.getCycleById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCycle_buildsNestedDaysAndPlannedExercises() {
        when(exerciseRepository.findById(5L)).thenReturn(Optional.of(exercise(5L)));
        when(trainingCycleRepository.save(any(TrainingCycle.class))).thenAnswer(inv -> {
            TrainingCycle c = inv.getArgument(0);
            c.setId(7L);
            return c;
        });

        PlannedExerciseRequest peReq = PlannedExerciseRequest.builder()
                .exerciseId(5L)
                .orderIndex(1)
                .sets(3)
                .repRange("8-10")
                .restPeriod("90s")
                .targetRpe(new BigDecimal("8"))
                .notes("note")
                .build();
        WorkoutDayRequest dayReq = WorkoutDayRequest.builder()
                .dayNumber(1)
                .name("Push")
                .notes("dn")
                .plannedExercises(List.of(peReq))
                .build();
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(1)
                .numberOfWeeks(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .notes("n")
                .workoutDays(List.of(dayReq))
                .build();

        TrainingCycleResponse result = trainingCycleService.createCycle(req);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getWorkoutDays()).hasSize(1);
        assertThat(result.getWorkoutDays().get(0).getPlannedExercises()).hasSize(1);
        verify(exerciseRepository).findById(5L);
    }

    @Test
    void createCycle_withNullWorkoutDays_skipsDayBuild() {
        when(trainingCycleRepository.save(any(TrainingCycle.class))).thenAnswer(inv -> {
            TrainingCycle c = inv.getArgument(0);
            c.setId(8L);
            return c;
        });

        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(2)
                .numberOfWeeks(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .workoutDays(null)
                .build();

        TrainingCycleResponse result = trainingCycleService.createCycle(req);

        assertThat(result.getId()).isEqualTo(8L);
        assertThat(result.getWorkoutDays()).isEmpty();
        verify(exerciseRepository, never()).findById(any());
    }

    @Test
    void createCycle_throws_whenExerciseMissing() {
        when(exerciseRepository.findById(404L)).thenReturn(Optional.empty());

        PlannedExerciseRequest peReq = PlannedExerciseRequest.builder()
                .exerciseId(404L)
                .orderIndex(1)
                .sets(3)
                .repRange("8")
                .restPeriod("60s")
                .build();
        WorkoutDayRequest dayReq = WorkoutDayRequest.builder()
                .dayNumber(1)
                .plannedExercises(List.of(peReq))
                .build();
        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(1)
                .numberOfWeeks(6)
                .startDate(LocalDate.of(2026, 1, 1))
                .workoutDays(List.of(dayReq))
                .build();

        assertThatThrownBy(() -> trainingCycleService.createCycle(req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(trainingCycleRepository, never()).save(any());
    }

    @Test
    void copyFromPreviousCycle_clonesDaysAndPlannedExercises() {
        TrainingCycle previous = cycleWithDayAndExercise();
        when(trainingCycleRepository.findTopByOrderByCycleNumberDesc()).thenReturn(Optional.of(previous));
        when(trainingCycleRepository.save(any(TrainingCycle.class))).thenAnswer(inv -> {
            TrainingCycle c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        TrainingCycleResponse result = trainingCycleService.copyFromPreviousCycle(
                LocalDate.of(2026, 3, 1), "next block");

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getCycleNumber()).isEqualTo(previous.getCycleNumber() + 1);
        assertThat(result.getNumberOfWeeks()).isEqualTo(previous.getNumberOfWeeks());
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.getNotes()).isEqualTo("next block");
        assertThat(result.getWorkoutDays()).hasSize(1);
        assertThat(result.getWorkoutDays().get(0).getPlannedExercises()).hasSize(1);
    }

    @Test
    void copyFromPreviousCycle_throws_whenNoPrevious() {
        when(trainingCycleRepository.findTopByOrderByCycleNumberDesc()).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                trainingCycleService.copyFromPreviousCycle(LocalDate.now(), "x"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No previous cycle");
    }

    @Test
    void updateCycle_updates_whenFound() {
        TrainingCycle cycle = cycleWithDayAndExercise();
        when(trainingCycleRepository.findById(1L)).thenReturn(Optional.of(cycle));
        when(trainingCycleRepository.save(any(TrainingCycle.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingCycleRequest req = TrainingCycleRequest.builder()
                .cycleNumber(2)
                .numberOfWeeks(8)
                .startDate(LocalDate.of(2026, 2, 1))
                .notes("updated")
                .build();

        TrainingCycleResponse result = trainingCycleService.updateCycle(1L, req);

        assertThat(result.getCycleNumber()).isEqualTo(2);
        assertThat(result.getNumberOfWeeks()).isEqualTo(8);
        assertThat(result.getNotes()).isEqualTo("updated");
    }

    @Test
    void updateCycle_throws_whenMissing() {
        when(trainingCycleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                trainingCycleService.updateCycle(99L, TrainingCycleRequest.builder()
                        .cycleNumber(1)
                        .numberOfWeeks(6)
                        .startDate(LocalDate.now())
                        .build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void endCycle_setsEndDate() {
        TrainingCycle cycle = cycleWithDayAndExercise();
        when(trainingCycleRepository.findById(1L)).thenReturn(Optional.of(cycle));
        when(trainingCycleRepository.save(any(TrainingCycle.class))).thenAnswer(inv -> inv.getArgument(0));

        TrainingCycleResponse result = trainingCycleService.endCycle(1L);

        assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void endCycle_throws_whenMissing() {
        when(trainingCycleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingCycleService.endCycle(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCycle_removes_whenExists() {
        when(trainingCycleRepository.existsById(1L)).thenReturn(true);

        trainingCycleService.deleteCycle(1L);

        verify(trainingCycleRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCycle_throws_whenMissing() {
        when(trainingCycleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> trainingCycleService.deleteCycle(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(trainingCycleRepository, never()).deleteById(any());
    }
}
