package com.bodoczky.fittracker.model;

import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExerciseCategory category;

    private String description;
}
