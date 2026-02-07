package com.bodoczky.fittracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "planned_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Integer sets;

    @Column(nullable = false)
    private String repRange;

    @Column(nullable = false)
    private String restPeriod;

    private BigDecimal targetRpe;

    @Column(length = 1000)
    private String notes;
}
