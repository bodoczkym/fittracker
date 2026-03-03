package com.bodoczky.fittracker.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDayResponse {

    private Long id;
    private Integer dayNumber;
    private String name;
    private String notes;
    private List<PlannedExerciseResponse> plannedExercises;
}
