package com.bodoczky.fittracker.repository;

import com.bodoczky.fittracker.model.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    List<ExerciseLog> findByWorkoutSessionIdOrderByOrderIndex(Long workoutSessionId);

    List<ExerciseLog> findByExerciseIdOrderByWorkoutSession_DateDesc(Long exerciseId);

    /**
     * Chronological history of one exercise across every session, most recent first. The date
     * bounds are optional — a null {@code from} or {@code to} drops that side of the range — so a
     * single query serves the unbounded, half-open, and closed-range cases. Sorted by session
     * date then log id descending for a stable order when several logs share a date.
     */
    @Query("""
            select l from ExerciseLog l
            where l.exercise.id = :exerciseId
              and (:from is null or l.workoutSession.date >= :from)
              and (:to is null or l.workoutSession.date <= :to)
            order by l.workoutSession.date desc, l.id desc
            """)
    List<ExerciseLog> findHistory(@Param("exerciseId") Long exerciseId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);
}
