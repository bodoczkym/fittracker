package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByWorkoutDayIdOrderByWeekNumber(Long workoutDayId);

    List<WorkoutSession> findByWorkoutDay_TrainingCycleIdOrderByDateDesc(Long trainingCycleId);
}
