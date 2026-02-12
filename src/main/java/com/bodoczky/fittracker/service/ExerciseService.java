package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.ExerciseResponse;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExerciseResponse getExerciseById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", id));
        return toResponse(exercise);
    }

    public List<ExerciseResponse> getExercisesByCategory(ExerciseCategory category) {
        return exerciseRepository.findByCategory(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExerciseResponse createExercise(ExerciseRequest request) {
        Exercise exercise = Exercise.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .build();
        return toResponse(exerciseRepository.save(exercise));
    }

    public ExerciseResponse updateExercise(Long id, ExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", id));

        exercise.setName(request.getName());
        exercise.setCategory(request.getCategory());
        exercise.setDescription(request.getDescription());

        return toResponse(exerciseRepository.save(exercise));
    }

    public void deleteExercise(Long id) {
        if (!exerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise", id);
        }
        exerciseRepository.deleteById(id);
    }

    private ExerciseResponse toResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .category(exercise.getCategory())
                .description(exercise.getDescription())
                .build();
    }
}
