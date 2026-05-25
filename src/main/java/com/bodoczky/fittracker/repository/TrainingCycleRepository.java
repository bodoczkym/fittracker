package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.TrainingCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingCycleRepository extends JpaRepository<TrainingCycle, Long> {

    Optional<TrainingCycle> findTopByOrderByCycleNumberDesc();

    /**
     * The single active cycle (the one with no end date). The one-active invariant
     * (ADR-0004) guarantees at most one row; more than one means the invariant was
     * violated, and the Optional return makes Spring Data surface that as a failure.
     */
    Optional<TrainingCycle> findByEndDateIsNull();
}
