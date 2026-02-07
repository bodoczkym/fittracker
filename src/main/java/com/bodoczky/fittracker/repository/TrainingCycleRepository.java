package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.TrainingCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingCycleRepository extends JpaRepository<TrainingCycle, Long> {

    Optional<TrainingCycle> findTopByOrderByCycleNumberDesc();
}
