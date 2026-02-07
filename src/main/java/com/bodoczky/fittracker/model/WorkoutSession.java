package com.bodoczky.fittracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_day_id", nullable = false)
    private WorkoutDay workoutDay;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private LocalDate date;

    private String location;

    private String notes;

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExerciseLog> exerciseLogs = new ArrayList<>();
}
