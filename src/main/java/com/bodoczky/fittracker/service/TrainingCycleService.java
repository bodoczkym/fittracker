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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public TrainingCycleResponse getActiveCycle() {
        TrainingCycle active = trainingCycleRepository.findByEndDateIsNull()
                .orElseThrow(() -> new ResourceNotFoundException("No active training cycle"));
        return toResponse(active);
    }

    @Transactional
    public TrainingCycleResponse createCycle(TrainingCycleRequest request) {
        endCurrentActiveCycle();

        TrainingCycle cycle = TrainingCycle.builder()
                .cycleNumber(request.getCycleNumber())
                .numberOfMicrocycles(request.getNumberOfMicrocycles())
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
                .orElseThrow(() -> new ResourceNotFoundException("No previous cycle found to copy from"));

        endCurrentActiveCycle();

        TrainingCycle newCycle = TrainingCycle.builder()
                .cycleNumber(previous.getCycleNumber() + 1)
                .numberOfMicrocycles(previous.getNumberOfMicrocycles())
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
        cycle.setNumberOfMicrocycles(request.getNumberOfMicrocycles());
        cycle.setStartDate(request.getStartDate());
        cycle.setNotes(request.getNotes());

        mergeWorkoutDays(cycle, request.getWorkoutDays());

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

    /**
     * Enforces the one-active-cycle invariant (ADR-0004): ends the currently-active
     * cycle (if any) by stamping today's end date, so the cycle created next becomes
     * the sole active one. This intentional side effect on another row is the point.
     */
    private void endCurrentActiveCycle() {
        trainingCycleRepository.findByEndDateIsNull().ifPresent(active -> {
            active.setEndDate(LocalDate.now());
            trainingCycleRepository.save(active);
        });
    }

    /**
     * Reconciles a cycle's workout days with a full PUT body, matching on {@code dayNumber}
     * (the natural key within a cycle, since requests carry no day id). Days present in both
     * are updated in place so their {@link WorkoutSession} rows survive; days dropped from the
     * request are orphan-removed along with their planned exercises; new day numbers are built
     * fresh.
     *
     * <p>Matching in place — rather than clearing and rebuilding the whole list — is
     * load-bearing. A recreated day gets a new id, and the cascade from {@link WorkoutDay} to
     * its sessions would then delete the frozen logs that ADR-0003 promises to preserve.
     *
     * <p>A PUT that omits {@code workoutDays} (null) leaves the existing template untouched, so
     * metadata-only edits don't silently wipe the plan; an explicit empty list clears every day.
     */
    private void mergeWorkoutDays(TrainingCycle cycle, List<WorkoutDayRequest> dayRequests) {
        if (dayRequests == null) {
            return;
        }

        Map<Integer, WorkoutDay> existingByDayNumber = cycle.getWorkoutDays().stream()
                .collect(Collectors.toMap(WorkoutDay::getDayNumber, Function.identity()));

        Set<Integer> requestedDayNumbers = dayRequests.stream()
                .map(WorkoutDayRequest::getDayNumber)
                .collect(Collectors.toSet());

        cycle.getWorkoutDays().removeIf(day -> !requestedDayNumbers.contains(day.getDayNumber()));

        for (WorkoutDayRequest dayRequest : dayRequests) {
            WorkoutDay existing = existingByDayNumber.get(dayRequest.getDayNumber());
            if (existing != null) {
                updateWorkoutDay(existing, dayRequest);
            } else {
                cycle.getWorkoutDays().add(buildWorkoutDay(dayRequest, cycle));
            }
        }
    }

    private void updateWorkoutDay(WorkoutDay day, WorkoutDayRequest request) {
        day.setName(request.getName());
        day.setNotes(request.getNotes());

        // Planned exercises are a pure template with nothing pointing back at them (ADR-0003),
        // so rebuilding the list wholesale is safe — historical logs are snapshots, not links.
        day.getPlannedExercises().clear();
        if (request.getPlannedExercises() != null) {
            for (PlannedExerciseRequest peRequest : request.getPlannedExercises()) {
                day.getPlannedExercises().add(buildPlannedExercise(peRequest, day));
            }
        }
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
                day.getPlannedExercises().add(buildPlannedExercise(peRequest, day));
            }
        }

        return day;
    }

    private PlannedExercise buildPlannedExercise(PlannedExerciseRequest request, WorkoutDay day) {
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.getExerciseId()));

        return PlannedExercise.builder()
                .workoutDay(day)
                .exercise(exercise)
                .orderIndex(request.getOrderIndex())
                .sets(request.getSets())
                .repRange(request.getRepRange())
                .restPeriod(request.getRestPeriod())
                .targetRpe(request.getTargetRpe())
                .notes(request.getNotes())
                .build();
    }

    private TrainingCycleResponse toResponse(TrainingCycle cycle) {
        List<WorkoutDayResponse> dayResponses = cycle.getWorkoutDays()
                .stream()
                .map(this::toWorkoutDayResponse)
                .toList();

        return TrainingCycleResponse.builder()
                .id(cycle.getId())
                .cycleNumber(cycle.getCycleNumber())
                .numberOfMicrocycles(cycle.getNumberOfMicrocycles())
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
