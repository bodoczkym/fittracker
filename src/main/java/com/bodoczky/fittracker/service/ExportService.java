package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.ExportResponse;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.ExerciseLog;
import com.bodoczky.fittracker.model.PlannedExercise;
import com.bodoczky.fittracker.model.TrainingCycle;
import com.bodoczky.fittracker.model.WorkoutDay;
import com.bodoczky.fittracker.model.WorkoutSession;
import com.bodoczky.fittracker.repository.ExerciseRepository;
import com.bodoczky.fittracker.repository.TrainingCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TrainingCycleRepository trainingCycleRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * Builds a lossless backup of the whole database (issue #16). Runs in a single read-only
     * transaction so the deep lazy traversal loads against one consistent snapshot rather than
     * leaning on open-session-in-view.
     */
    @Transactional(readOnly = true)
    public ExportResponse exportAll() {
        return ExportResponse.builder()
                .exportedAt(Instant.now())
                .exercises(exerciseRepository.findAll().stream().map(this::toExercise).toList())
                .trainingCycles(trainingCycleRepository.findAll().stream().map(this::toCycle).toList())
                .build();
    }

    private ExportResponse.Exercise toExercise(Exercise exercise) {
        return ExportResponse.Exercise.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .category(exercise.getCategory())
                .description(exercise.getDescription())
                .build();
    }

    private ExportResponse.TrainingCycle toCycle(TrainingCycle cycle) {
        return ExportResponse.TrainingCycle.builder()
                .id(cycle.getId())
                .cycleNumber(cycle.getCycleNumber())
                .numberOfMicrocycles(cycle.getNumberOfMicrocycles())
                .startDate(cycle.getStartDate())
                .endDate(cycle.getEndDate())
                .notes(cycle.getNotes())
                .workoutDays(cycle.getWorkoutDays().stream().map(this::toWorkoutDay).toList())
                .build();
    }

    private ExportResponse.WorkoutDay toWorkoutDay(WorkoutDay day) {
        return ExportResponse.WorkoutDay.builder()
                .id(day.getId())
                .dayNumber(day.getDayNumber())
                .name(day.getName())
                .notes(day.getNotes())
                .plannedExercises(day.getPlannedExercises().stream().map(this::toPlannedExercise).toList())
                .workoutSessions(day.getWorkoutSessions().stream().map(this::toWorkoutSession).toList())
                .build();
    }

    private ExportResponse.PlannedExercise toPlannedExercise(PlannedExercise pe) {
        return ExportResponse.PlannedExercise.builder()
                .id(pe.getId())
                .exerciseId(pe.getExercise().getId())
                .orderIndex(pe.getOrderIndex())
                .sets(pe.getSets())
                .repRange(pe.getRepRange())
                .restPeriod(pe.getRestPeriod())
                .targetRpe(pe.getTargetRpe())
                .notes(pe.getNotes())
                .build();
    }

    private ExportResponse.WorkoutSession toWorkoutSession(WorkoutSession session) {
        return ExportResponse.WorkoutSession.builder()
                .id(session.getId())
                .microcycleNumber(session.getMicrocycleNumber())
                .date(session.getDate())
                .location(session.getLocation())
                .notes(session.getNotes())
                .exerciseLogs(session.getExerciseLogs().stream().map(this::toExerciseLog).toList())
                .build();
    }

    private ExportResponse.ExerciseLog toExerciseLog(ExerciseLog log) {
        return ExportResponse.ExerciseLog.builder()
                .id(log.getId())
                .exerciseId(log.getExercise().getId())
                .planned(log.getPlanned())
                .orderIndex(log.getOrderIndex())
                .sets(log.getSets())
                .repRange(log.getRepRange())
                .restPeriod(log.getRestPeriod())
                .targetRpe(log.getTargetRpe())
                .actualPerformance(log.getActualPerformance())
                .actualRpe(log.getActualRpe())
                .notes(log.getNotes())
                .build();
    }
}
