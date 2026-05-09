package com.bodoczky.fittracker.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseLogResponse {

    private Long id;
    private ExerciseResponse exercise;
    private Boolean planned;
    private Integer orderIndex;
    private Integer sets;
    private String repRange;
    private String restPeriod;
    private BigDecimal targetRpe;
    private String actualPerformance;
    private BigDecimal actualRpe;
    private String notes;
}
