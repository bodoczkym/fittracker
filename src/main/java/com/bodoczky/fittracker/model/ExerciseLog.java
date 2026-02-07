package com.bodoczky.fittracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "exercise_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Boolean planned;

    @Column(nullable = false)
    private Integer orderIndex;

    private Integer sets;

    private String repRange;

    private String restPeriod;

    private BigDecimal targetRpe;

    @Column(length = 500)
    private String actualPerformance;

    private BigDecimal actualRpe;

    @Column(length = 1000)
    private String notes;
}
