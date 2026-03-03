package com.bodoczky.fittracker.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedExerciseResponse {

    private Long id;
    private ExerciseResponse exercise;
    private Integer orderIndex;
    private Integer sets;
    private String repRange;
    private String restPeriod;
    private BigDecimal targetRpe;
    private String notes;
}
