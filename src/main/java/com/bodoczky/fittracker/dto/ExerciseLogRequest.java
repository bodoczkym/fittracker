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
public class ExerciseLogRequest {

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @NotNull(message = "Planned flag is required")
    private Boolean planned;

    @NotNull(message = "Order index is required")
    @Positive
    private Integer orderIndex;

    private Integer sets;

    private String repRange;

    private String restPeriod;

    private BigDecimal targetRpe;

    private String actualPerformance;

    private BigDecimal actualRpe;

    private String notes;
}
