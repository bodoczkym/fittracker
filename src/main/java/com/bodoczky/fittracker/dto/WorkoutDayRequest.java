package com.bodoczky.fittracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDayRequest {

    @NotNull(message = "Day number is required")
    @Positive
    private Integer dayNumber;

    private String name;

    private String notes;

    @Valid
    private List<PlannedExerciseRequest> plannedExercises;
}
