package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.WorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDay, Long> {

    List<WorkoutDay> findByTrainingCycleIdOrderByDayNumber(Long trainingCycleId);
}
