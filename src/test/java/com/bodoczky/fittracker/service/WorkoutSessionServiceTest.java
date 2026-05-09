package com.bodoczky.fittracker.service;

import com.bodoczky.fittracker.dto.ExerciseLogRequest;
import com.bodoczky.fittracker.dto.ExerciseLogResponse;
import com.bodoczky.fittracker.dto.WorkoutSessionRequest;
import com.bodoczky.fittracker.dto.WorkoutSessionResponse;
import com.bodoczky.fittracker.exception.ResourceNotFoundException;
import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.ExerciseLog;
import com.bodoczky.fittracker.model.PlannedExercise;
import com.bodoczky.fittracker.model.TrainingCycle;
import com.bodoczky.fittracker.model.WorkoutDay;
import com.bodoczky.fittracker.model.WorkoutSession;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.repository.ExerciseLogRepository;
import com.bodoczky.fittracker.repository.ExerciseRepository;
import com.bodoczky.fittracker.repository.WorkoutDayRepository;
import com.bodoczky.fittracker.repository.WorkoutSessionRepository;
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
class WorkoutSessionServiceTest {

    @Mock
    private WorkoutSessionRepository workoutSessionRepository;

    @Mock
    private WorkoutDayRepository workoutDayRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @InjectMocks
    private WorkoutSessionService workoutSessionService;

    private Exercise exercise(long id) {
        return Exercise.builder()
                .id(id)
                .name("Ex" + id)
                .category(ExerciseCategory.BENCH_PRESS)
                .build();
    }

    private WorkoutDay dayWithPlannedExercise() {
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
                .exercise(exercise(5L))
                .orderIndex(1)
                .sets(3)
                .repRange("8")
                .restPeriod("90s")
                .build();
        day.getPlannedExercises().add(pe);
        return day;
    }

    private WorkoutSession sessionWithLog() {
        WorkoutDay day = dayWithPlannedExercise();
        WorkoutSession session = WorkoutSession.builder()
                .id(50L)
                .workoutDay(day)
                .weekNumber(1)
                .date(LocalDate.of(2026, 1, 5))
                .build();
        ExerciseLog log = ExerciseLog.builder()
                .id(500L)
                .workoutSession(session)
                .exercise(exercise(5L))
                .planned(true)
                .orderIndex(1)
                .sets(3)
                .repRange("8")
                .restPeriod("90s")
                .build();
        session.getExerciseLogs().add(log);
        return session;
    }

    @Test
    void getSessionsByCycle_returnsMappedList() {
        when(workoutSessionRepository.findByWorkoutDay_TrainingCycleIdOrderByDateDesc(1L))
                .thenReturn(List.of(sessionWithLog()));

        List<WorkoutSessionResponse> result = workoutSessionService.getSessionsByCycle(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(50L);
        assertThat(result.get(0).getExerciseLogs()).hasSize(1);
    }

    @Test
    void getSessionsByCycleAndWeek_returnsMappedList() {
        when(workoutSessionRepository.findByWorkoutDay_TrainingCycleIdAndWeekNumberOrderByDateDesc(1L, 2))
                .thenReturn(List.of(sessionWithLog()));

        List<WorkoutSessionResponse> result = workoutSessionService.getSessionsByCycleAndWeek(1L, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWeekNumber()).isEqualTo(1);
    }

    @Test
    void getSessionById_returns_whenFound() {
        when(workoutSessionRepository.findById(50L)).thenReturn(Optional.of(sessionWithLog()));

        WorkoutSessionResponse result = workoutSessionService.getSessionById(50L);

        assertThat(result.getId()).isEqualTo(50L);
    }

    @Test
    void getSessionById_throws_whenMissing() {
        when(workoutSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workoutSessionService.getSessionById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSession_clonesPlannedExercisesIntoLogs() {
        WorkoutDay day = dayWithPlannedExercise();
        when(workoutDayRepository.findById(10L)).thenReturn(Optional.of(day));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(inv -> {
            WorkoutSession s = inv.getArgument(0);
            s.setId(60L);
            return s;
        });

        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(10L)
                .weekNumber(1)
                .date(LocalDate.of(2026, 1, 5))
                .location("Home Gym")
                .notes("good day")
                .build();

        WorkoutSessionResponse result = workoutSessionService.createSession(req);

        assertThat(result.getId()).isEqualTo(60L);
        assertThat(result.getWorkoutDayId()).isEqualTo(10L);
        assertThat(result.getExerciseLogs()).hasSize(1);
        assertThat(result.getExerciseLogs().get(0).getPlanned()).isTrue();
    }

    @Test
    void createSession_throws_whenDayMissing() {
        when(workoutDayRepository.findById(99L)).thenReturn(Optional.empty());

        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(99L)
                .weekNumber(1)
                .date(LocalDate.now())
                .build();

        assertThatThrownBy(() -> workoutSessionService.createSession(req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutSessionRepository, never()).save(any());
    }

    @Test
    void updateSession_keepsDay_whenSameId() {
        WorkoutSession session = sessionWithLog();
        when(workoutSessionRepository.findById(50L)).thenReturn(Optional.of(session));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(10L)
                .weekNumber(2)
                .date(LocalDate.of(2026, 1, 12))
                .location("Home")
                .notes("updated")
                .build();

        WorkoutSessionResponse result = workoutSessionService.updateSession(50L, req);

        assertThat(result.getWeekNumber()).isEqualTo(2);
        assertThat(result.getNotes()).isEqualTo("updated");
        verify(workoutDayRepository, never()).findById(any());
    }

    @Test
    void updateSession_switchesDay_whenIdChanged() {
        WorkoutSession session = sessionWithLog();
        WorkoutDay newDay = WorkoutDay.builder()
                .id(11L)
                .trainingCycle(session.getWorkoutDay().getTrainingCycle())
                .dayNumber(2)
                .build();
        when(workoutSessionRepository.findById(50L)).thenReturn(Optional.of(session));
        when(workoutDayRepository.findById(11L)).thenReturn(Optional.of(newDay));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(11L)
                .weekNumber(1)
                .date(LocalDate.now())
                .build();

        WorkoutSessionResponse result = workoutSessionService.updateSession(50L, req);

        assertThat(result.getWorkoutDayId()).isEqualTo(11L);
    }

    @Test
    void updateSession_throws_whenSessionMissing() {
        when(workoutSessionRepository.findById(99L)).thenReturn(Optional.empty());

        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(10L)
                .weekNumber(1)
                .date(LocalDate.now())
                .build();

        assertThatThrownBy(() -> workoutSessionService.updateSession(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSession_throws_whenNewDayMissing() {
        WorkoutSession session = sessionWithLog();
        when(workoutSessionRepository.findById(50L)).thenReturn(Optional.of(session));
        when(workoutDayRepository.findById(404L)).thenReturn(Optional.empty());

        WorkoutSessionRequest req = WorkoutSessionRequest.builder()
                .workoutDayId(404L)
                .weekNumber(1)
                .date(LocalDate.now())
                .build();

        assertThatThrownBy(() -> workoutSessionService.updateSession(50L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteSession_removes_whenExists() {
        when(workoutSessionRepository.existsById(50L)).thenReturn(true);

        workoutSessionService.deleteSession(50L);

        verify(workoutSessionRepository, times(1)).deleteById(50L);
    }

    @Test
    void deleteSession_throws_whenMissing() {
        when(workoutSessionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> workoutSessionService.deleteSession(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addLog_persistsAndReturns() {
        WorkoutSession session = sessionWithLog();
        Exercise ex = exercise(7L);
        when(workoutSessionRepository.findById(50L)).thenReturn(Optional.of(session));
        when(exerciseRepository.findById(7L)).thenReturn(Optional.of(ex));
        when(exerciseLogRepository.save(any(ExerciseLog.class))).thenAnswer(inv -> {
            ExerciseLog l = inv.getArgument(0);
            l.setId(900L);
            return l;
        });

        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(7L)
                .planned(false)
                .orderIndex(2)
                .sets(4)
                .repRange("10")
                .restPeriod("60s")
                .targetRpe(new BigDecimal("8"))
                .actualPerformance("70*10,9,8")
                .actualRpe(new BigDecimal("9"))
                .notes("ad-hoc")
                .build();

        ExerciseLogResponse result = workoutSessionService.addLog(50L, req);

        assertThat(result.getId()).isEqualTo(900L);
        assertThat(result.getPlanned()).isFalse();
        assertThat(result.getActualPerformance()).isEqualTo("70*10,9,8");
    }

    @Test
    void addLog_throws_whenSessionMissing() {
        when(workoutSessionRepository.findById(99L)).thenReturn(Optional.empty());

        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(7L)
                .planned(false)
                .orderIndex(1)
                .build();

        assertThatThrownBy(() -> workoutSessionService.addLog(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addLog_throws_whenExerciseMissing() {
        when(workoutSessionRepository.findById(50L)).thenReturn(Optional.of(sessionWithLog()));
        when(exerciseRepository.findById(404L)).thenReturn(Optional.empty());

        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(404L)
                .planned(true)
                .orderIndex(1)
                .build();

        assertThatThrownBy(() -> workoutSessionService.addLog(50L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateLog_keepsExercise_whenSameId() {
        ExerciseLog log = ExerciseLog.builder()
                .id(500L)
                .workoutSession(sessionWithLog())
                .exercise(exercise(5L))
                .planned(true)
                .orderIndex(1)
                .build();
        when(exerciseLogRepository.findById(500L)).thenReturn(Optional.of(log));
        when(exerciseLogRepository.save(any(ExerciseLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(5L)
                .planned(true)
                .orderIndex(1)
                .sets(4)
                .repRange("8")
                .restPeriod("90s")
                .actualPerformance("80*8,8,7")
                .build();

        ExerciseLogResponse result = workoutSessionService.updateLog(500L, req);

        assertThat(result.getActualPerformance()).isEqualTo("80*8,8,7");
        verify(exerciseRepository, never()).findById(any());
    }

    @Test
    void updateLog_switchesExercise_whenIdChanged() {
        ExerciseLog log = ExerciseLog.builder()
                .id(500L)
                .workoutSession(sessionWithLog())
                .exercise(exercise(5L))
                .planned(true)
                .orderIndex(1)
                .build();
        when(exerciseLogRepository.findById(500L)).thenReturn(Optional.of(log));
        when(exerciseRepository.findById(8L)).thenReturn(Optional.of(exercise(8L)));
        when(exerciseLogRepository.save(any(ExerciseLog.class))).thenAnswer(inv -> inv.getArgument(0));

        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(8L)
                .planned(false)
                .orderIndex(1)
                .build();

        ExerciseLogResponse result = workoutSessionService.updateLog(500L, req);

        assertThat(result.getExercise().getId()).isEqualTo(8L);
        assertThat(result.getPlanned()).isFalse();
    }

    @Test
    void updateLog_throws_whenLogMissing() {
        when(exerciseLogRepository.findById(99L)).thenReturn(Optional.empty());

        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(5L)
                .planned(true)
                .orderIndex(1)
                .build();

        assertThatThrownBy(() -> workoutSessionService.updateLog(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateLog_throws_whenNewExerciseMissing() {
        ExerciseLog log = ExerciseLog.builder()
                .id(500L)
                .workoutSession(sessionWithLog())
                .exercise(exercise(5L))
                .planned(true)
                .orderIndex(1)
                .build();
        when(exerciseLogRepository.findById(500L)).thenReturn(Optional.of(log));
        when(exerciseRepository.findById(404L)).thenReturn(Optional.empty());

        ExerciseLogRequest req = ExerciseLogRequest.builder()
                .exerciseId(404L)
                .planned(true)
                .orderIndex(1)
                .build();

        assertThatThrownBy(() -> workoutSessionService.updateLog(500L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteLog_removes_whenExists() {
        when(exerciseLogRepository.existsById(500L)).thenReturn(true);

        workoutSessionService.deleteLog(500L);

        verify(exerciseLogRepository, times(1)).deleteById(500L);
    }

    @Test
    void deleteLog_throws_whenMissing() {
        when(exerciseLogRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> workoutSessionService.deleteLog(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
