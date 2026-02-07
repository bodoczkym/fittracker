package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.PlannedExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlannedExerciseRepository extends JpaRepository<PlannedExercise, Long> {

    List<PlannedExercise> findByWorkoutDayIdOrderByOrderIndex(Long workoutDayId);
}
