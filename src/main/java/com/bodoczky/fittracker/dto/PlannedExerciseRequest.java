package com.bodoczky.fittracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedExerciseRequest {

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @NotNull(message = "Order index is required")
    @Positive
    private Integer orderIndex;

    @NotNull(message = "Number of sets is required")
    @Positive
    private Integer sets;

    @NotNull(message = "Rep range is required")
    private String repRange;

    @NotNull(message = "Rest period is required")
    private String restPeriod;

    private BigDecimal targetRpe;

    private String notes;
}
