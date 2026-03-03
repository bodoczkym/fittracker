package com.bodoczky.fittracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingCycleRequest {

    @NotNull(message = "Cycle number is required")
    @Positive
    private Integer cycleNumber;

    @Builder.Default
    private Integer numberOfWeeks = 6;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private String notes;

    @Valid
    private List<WorkoutDayRequest> workoutDays;
}
