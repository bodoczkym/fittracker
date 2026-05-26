package com.bodoczky.fittracker.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of an exercise's history: the actual performance logged for the exercise in a single
 * workout session, flattened with the session/cycle coordinates needed to place it in time.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseHistoryEntryResponse {

    private LocalDate sessionDate;
    private Long trainingCycleId;
    private Integer cycleNumber;
    private Integer microcycleNumber;
    private String actualPerformance;
    private BigDecimal actualRpe;
    private String notes;
}
