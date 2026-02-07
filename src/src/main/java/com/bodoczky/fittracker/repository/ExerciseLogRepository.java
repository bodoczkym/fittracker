package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    List<ExerciseLog> findByWorkoutSessionIdOrderByOrderIndex(Long workoutSessionId);

    List<ExerciseLog> findByExerciseIdOrderByWorkoutSession_DateDesc(Long exerciseId);
}
