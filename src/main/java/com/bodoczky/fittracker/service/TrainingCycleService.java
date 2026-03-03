package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.*;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.*;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingCycleService {

    private final TrainingCycleRepository trainingCycleRepository;
    private final ExerciseRepository exerciseRepository;

    public List<TrainingCycleResponse> getAllCycles() {
        return trainingCycleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TrainingCycleResponse getCycleById(Long id) {
        TrainingCycle cycle = trainingCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingCycle", id));
        return toResponse(cycle);
    }

    @Transactional
    public TrainingCycleResponse createCycle(TrainingCycleRequest request) {
        TrainingCycle cycle = TrainingCycle.builder()
                .cycleNumber(request.getCycleNumber())
                .numberOfWeeks(request.getNumberOfWeeks())
                .startDate(request.getStartDate())
                .notes(request.getNotes())
                .build();

        if (request.getWorkoutDays() != null) {
            for (WorkoutDayRequest dayRequest : request.getWorkoutDays()) {
                WorkoutDay day = buildWorkoutDay(dayRequest, cycle);
                cycle.getWorkoutDays().add(day);
            }
        }

        return toResponse(trainingCycleRepository.save(cycle));
    }

    @Transactional
    public TrainingCycleResponse copyFromPreviousCycle(LocalDate startDate, String notes) {
        TrainingCycle previous = trainingCycleRepository.findTopByOrderByCycleNumberDesc()
                .orElseThrow(() -> new RuntimeException("No previous cycle found to copy"));

        TrainingCycle newCycle = TrainingCycle.builder()
                .cycleNumber(previous.getCycleNumber() + 1)
                .numberOfWeeks(previous.getNumberOfWeeks())
                .startDate(startDate)
                .notes(notes)
                .build();

        for (WorkoutDay previousDay : previous.getWorkoutDays()) {
            WorkoutDay newDay = WorkoutDay.builder()
                    .trainingCycle(newCycle)
                    .dayNumber(previousDay.getDayNumber())
                    .name(previousDay.getName())
                    .notes(previousDay.getNotes())
                    .build();

            for (PlannedExercise previousExercise : previousDay.getPlannedExercises()) {
                PlannedExercise newExercise = PlannedExercise.builder()
                        .workoutDay(newDay)
                        .exercise(previousExercise.getExercise())
                        .orderIndex(previousExercise.getOrderIndex())
                        .sets(previousExercise.getSets())
                        .repRange(previousExercise.getRepRange())
                        .restPeriod(previousExercise.getRestPeriod())
                        .targetRpe(previousExercise.getTargetRpe())
                        .notes(previousExercise.getNotes())
                        .build();
                newDay.getPlannedExercises().add(newExercise);
            }

            newCycle.getWorkoutDays().add(newDay);
        }

        return toResponse(trainingCycleRepository.save(newCycle));
    }

    @Transactional
    public TrainingCycleResponse updateCycle(Long id, TrainingCycleRequest request) {
        TrainingCycle cycle = trainingCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingCycle", id));

        cycle.setCycleNumber(request.getCycleNumber());
        cycle.setNumberOfWeeks(request.getNumberOfWeeks());
        cycle.setStartDate(request.getStartDate());
        cycle.setNotes(request.getNotes());

        return toResponse(trainingCycleRepository.save(cycle));
    }

    @Transactional
    public TrainingCycleResponse endCycle(Long id) {
        TrainingCycle cycle = trainingCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingCycle", id));
        cycle.setEndDate(LocalDate.now());
        return toResponse(trainingCycleRepository.save(cycle));
    }

    public void deleteCycle(Long id) {
        if (!trainingCycleRepository.existsById(id)) {
            throw new ResourceNotFoundException("TrainingCycle", id);
        }
        trainingCycleRepository.deleteById(id);
    }

    private WorkoutDay buildWorkoutDay(WorkoutDayRequest request, TrainingCycle cycle) {
        WorkoutDay day = WorkoutDay.builder()
                .trainingCycle(cycle)
                .dayNumber(request.getDayNumber())
                .name(request.getName())
                .notes(request.getNotes())
                .build();

        if (request.getPlannedExercises() != null) {
            for (PlannedExerciseRequest peRequest : request.getPlannedExercises()) {
                Exercise exercise = exerciseRepository.findById(peRequest.getExerciseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Exercise", peRequest.getExerciseId()));

                PlannedExercise plannedExercise = PlannedExercise.builder()
                        .workoutDay(day)
                        .exercise(exercise)
                        .orderIndex(peRequest.getOrderIndex())
                        .sets(peRequest.getSets())
                        .repRange(peRequest.getRepRange())
                        .restPeriod(peRequest.getRestPeriod())
                        .targetRpe(peRequest.getTargetRpe())
                        .notes(peRequest.getNotes())
                        .build();
                day.getPlannedExercises().add(plannedExercise);
            }
        }

        return day;
    }

    private TrainingCycleResponse toResponse(TrainingCycle cycle) {
        List<WorkoutDayResponse> dayResponses = cycle.getWorkoutDays()
                .stream()
                .map(this::toWorkoutDayResponse)
                .toList();

        return TrainingCycleResponse.builder()
                .id(cycle.getId())
                .cycleNumber(cycle.getCycleNumber())
                .numberOfWeeks(cycle.getNumberOfWeeks())
                .startDate(cycle.getStartDate())
                .endDate(cycle.getEndDate())
                .notes(cycle.getNotes())
                .workoutDays(dayResponses)
                .build();
    }

    private WorkoutDayResponse toWorkoutDayResponse(WorkoutDay day) {
        List<PlannedExerciseResponse> exerciseResponses = day.getPlannedExercises()
                .stream()
                .map(this::toPlannedExerciseResponse)
                .toList();

        return WorkoutDayResponse.builder()
                .id(day.getId())
                .dayNumber(day.getDayNumber())
                .name(day.getName())
                .notes(day.getNotes())
                .plannedExercises(exerciseResponses)
                .build();
    }

    private PlannedExerciseResponse toPlannedExerciseResponse(PlannedExercise pe) {
        ExerciseResponse exerciseResponse = ExerciseResponse.builder()
                .id(pe.getExercise().getId())
                .name(pe.getExercise().getName())
                .category(pe.getExercise().getCategory())
                .description(pe.getExercise().getDescription())
                .build();

        return PlannedExerciseResponse.builder()
                .id(pe.getId())
                .exercise(exerciseResponse)
                .orderIndex(pe.getOrderIndex())
                .sets(pe.getSets())
                .repRange(pe.getRepRange())
                .restPeriod(pe.getRestPeriod())
                .targetRpe(pe.getTargetRpe())
                .notes(pe.getNotes())
                .build();
    }
}
