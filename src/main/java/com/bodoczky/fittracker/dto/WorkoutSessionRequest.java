package com.bodoczky.fittracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSessionRequest {

    @NotNull(message = "Workout day ID is required")
    private Long workoutDayId;

    @NotNull(message = "Microcycle number is required")
    @Positive
    private Integer microcycleNumber;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String location;

    private String notes;
}
