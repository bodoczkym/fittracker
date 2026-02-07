package com.bodoczky.fittracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_cycle_id", nullable = false)
    private TrainingCycle trainingCycle;

    @Column(nullable = false)
    private Integer dayNumber;

    private String name;

    private String notes;

    @OneToMany(mappedBy = "workoutDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlannedExercise> plannedExercises = new ArrayList<>();

    @OneToMany(mappedBy = "workoutDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutSession> workoutSessions = new ArrayList<>();
}
