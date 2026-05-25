package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.*;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.*;
import com.bodoczky.fittracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutDayRepository workoutDayRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseLogRepository exerciseLogRepository;

    public List<WorkoutSessionResponse> getSessionsByCycle(Long cycleId) {
        return workoutSessionRepository
                .findByWorkoutDay_TrainingCycleIdOrderByDateDesc(cycleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<WorkoutSessionResponse> getSessionsByCycleAndMicrocycle(Long cycleId, Integer microcycleNumber) {
        return workoutSessionRepository
                .findByWorkoutDay_TrainingCycleIdAndMicrocycleNumberOrderByDateDesc(cycleId, microcycleNumber)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkoutSessionResponse getSessionById(Long id) {
        WorkoutSession session = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", id));
        return toResponse(session);
    }

    @Transactional
    public WorkoutSessionResponse createSession(WorkoutSessionRequest request) {
        WorkoutDay day = workoutDayRepository.findById(request.getWorkoutDayId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", request.getWorkoutDayId()));

        WorkoutSession session = WorkoutSession.builder()
                .workoutDay(day)
                .microcycleNumber(request.getMicrocycleNumber())
                .date(request.getDate())
                .location(request.getLocation())
                .notes(request.getNotes())
                .build();

        for (PlannedExercise planned : day.getPlannedExercises()) {
            ExerciseLog log = ExerciseLog.builder()
                    .workoutSession(session)
                    .exercise(planned.getExercise())
                    .planned(true)
                    .orderIndex(planned.getOrderIndex())
                    .sets(planned.getSets())
                    .repRange(planned.getRepRange())
                    .restPeriod(planned.getRestPeriod())
                    .targetRpe(planned.getTargetRpe())
                    .notes(planned.getNotes())
                    .build();
            session.getExerciseLogs().add(log);
        }

        return toResponse(workoutSessionRepository.save(session));
    }

    @Transactional
    public WorkoutSessionResponse updateSession(Long id, WorkoutSessionRequest request) {
        WorkoutSession session = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", id));

        if (!session.getWorkoutDay().getId().equals(request.getWorkoutDayId())) {
            WorkoutDay day = workoutDayRepository.findById(request.getWorkoutDayId())
                    .orElseThrow(() -> new ResourceNotFoundException("WorkoutDay", request.getWorkoutDayId()));
            session.setWorkoutDay(day);
        }

        session.setMicrocycleNumber(request.getMicrocycleNumber());
        session.setDate(request.getDate());
        session.setLocation(request.getLocation());
        session.setNotes(request.getNotes());

        return toResponse(workoutSessionRepository.save(session));
    }

    public void deleteSession(Long id) {
        if (!workoutSessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkoutSession", id);
        }
        workoutSessionRepository.deleteById(id);
    }

    @Transactional
    public ExerciseLogResponse addLog(Long sessionId, ExerciseLogRequest request) {
        WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", sessionId));

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.getExerciseId()));

        ExerciseLog log = ExerciseLog.builder()
                .workoutSession(session)
                .exercise(exercise)
                .planned(request.getPlanned())
                .orderIndex(request.getOrderIndex())
                .sets(request.getSets())
                .repRange(request.getRepRange())
                .restPeriod(request.getRestPeriod())
                .targetRpe(request.getTargetRpe())
                .actualPerformance(request.getActualPerformance())
                .actualRpe(request.getActualRpe())
                .notes(request.getNotes())
                .build();

        session.getExerciseLogs().add(log);
        return toLogResponse(exerciseLogRepository.save(log));
    }

    @Transactional
    public ExerciseLogResponse updateLog(Long logId, ExerciseLogRequest request) {
        ExerciseLog log = exerciseLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("ExerciseLog", logId));

        if (!log.getExercise().getId().equals(request.getExerciseId())) {
            Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exercise", request.getExerciseId()));
            log.setExercise(exercise);
        }

        log.setPlanned(request.getPlanned());
        log.setOrderIndex(request.getOrderIndex());
        log.setSets(request.getSets());
        log.setRepRange(request.getRepRange());
        log.setRestPeriod(request.getRestPeriod());
        log.setTargetRpe(request.getTargetRpe());
        log.setActualPerformance(request.getActualPerformance());
        log.setActualRpe(request.getActualRpe());
        log.setNotes(request.getNotes());

        return toLogResponse(exerciseLogRepository.save(log));
    }

    public void deleteLog(Long logId) {
        if (!exerciseLogRepository.existsById(logId)) {
            throw new ResourceNotFoundException("ExerciseLog", logId);
        }
        exerciseLogRepository.deleteById(logId);
    }

    private WorkoutSessionResponse toResponse(WorkoutSession session) {
        List<ExerciseLogResponse> logResponses = session.getExerciseLogs()
                .stream()
                .map(this::toLogResponse)
                .toList();

        return WorkoutSessionResponse.builder()
                .id(session.getId())
                .workoutDayId(session.getWorkoutDay().getId())
                .microcycleNumber(session.getMicrocycleNumber())
                .date(session.getDate())
                .location(session.getLocation())
                .notes(session.getNotes())
                .exerciseLogs(logResponses)
                .build();
    }

    private ExerciseLogResponse toLogResponse(ExerciseLog log) {
        ExerciseResponse exerciseResponse = ExerciseResponse.builder()
                .id(log.getExercise().getId())
                .name(log.getExercise().getName())
                .category(log.getExercise().getCategory())
                .description(log.getExercise().getDescription())
                .build();

        return ExerciseLogResponse.builder()
                .id(log.getId())
                .exercise(exerciseResponse)
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
