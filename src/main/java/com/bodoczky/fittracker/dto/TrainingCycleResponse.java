package com.bodoczky.fittracker.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingCycleResponse {

    private Long id;
    private Integer cycleNumber;
    private Integer numberOfWeeks;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private List<WorkoutDayResponse> workoutDays;
}
