package com.bodoczky.fittracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cycleNumber;

    @Column(nullable = false)
    @Builder.Default
    private Integer numberOfWeeks = 6;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private String notes;

    @OneToMany(mappedBy = "trainingCycle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkoutDay> workoutDays = new ArrayList<>();
}
