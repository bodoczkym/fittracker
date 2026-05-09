package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.ExerciseResponse;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.repository.ExerciseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
