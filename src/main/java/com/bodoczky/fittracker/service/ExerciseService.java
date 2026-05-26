package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.ExerciseHistoryEntryResponse;
import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.ExerciseResponse;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.ExerciseLog;
import com.bodoczky.fittracker.model.TrainingCycle;
import com.bodoczky.fittracker.model.WorkoutSession;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.repository.ExerciseLogRepository;
import com.bodoczky.fittracker.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseLogRepository exerciseLogRepository;

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

    /**
     * Returns the chronological history of an exercise — its logged performances across all
     * sessions, most recent first — optionally bounded by session date. A missing exercise is a
     * 404; an exercise with no logs in range is an empty list, not an error.
     */
    public List<ExerciseHistoryEntryResponse> getExerciseHistory(Long id, LocalDate from, LocalDate to) {
        if (!exerciseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exercise", id);
        }
        return exerciseLogRepository.findHistory(id, from, to)
                .stream()
                .map(this::toHistoryEntry)
                .toList();
    }

    private ExerciseHistoryEntryResponse toHistoryEntry(ExerciseLog log) {
        WorkoutSession session = log.getWorkoutSession();
        TrainingCycle cycle = session.getWorkoutDay().getTrainingCycle();
        return ExerciseHistoryEntryResponse.builder()
                .sessionDate(session.getDate())
                .trainingCycleId(cycle.getId())
                .cycleNumber(cycle.getCycleNumber())
                .microcycleNumber(session.getMicrocycleNumber())
                .actualPerformance(log.getActualPerformance())
                .actualRpe(log.getActualRpe())
                .notes(log.getNotes())
                .build();
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
