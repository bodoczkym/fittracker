package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.Exercise;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByCategory(ExerciseCategory category);

    List<Exercise> findByNameContainingIgnoreCase(String name);
}
