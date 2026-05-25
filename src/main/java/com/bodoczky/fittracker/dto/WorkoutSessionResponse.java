package com.bodoczky.fittracker.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSessionResponse {

    private Long id;
    private Long workoutDayId;
    private Integer microcycleNumber;
    private LocalDate date;
    private String location;
    private String notes;
    private List<ExerciseLogResponse> exerciseLogs;
}
