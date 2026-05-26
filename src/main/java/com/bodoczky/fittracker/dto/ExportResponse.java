package com.bodoczky.fittracker.dto;

import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Full-database backup document for {@code GET /api/v1/export} (issue #16). A lossless snapshot:
 * the exercise catalog plus every training cycle with its nested workout days, planned exercises,
 * workout sessions, and exercise logs. Catalog references inside planned exercises and logs are
 * kept as bare {@code exerciseId} values rather than re-nesting the exercise, so a re-import can
 * wire them back to the single {@code exercises} list.
 *
 * <p>Modelled as one nested tree rather than scattered DTOs because it is a single aggregate with
 * no use outside export, and the nesting mirrors the document the client receives.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportResponse {

    private Instant exportedAt;
    private List<Exercise> exercises;
    private List<TrainingCycle> trainingCycles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Exercise {
        private Long id;
        private String name;
        private ExerciseCategory category;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrainingCycle {
        private Long id;
        private Integer cycleNumber;
        private Integer numberOfMicrocycles;
        private LocalDate startDate;
        private LocalDate endDate;
        private String notes;
        private List<WorkoutDay> workoutDays;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkoutDay {
        private Long id;
        private Integer dayNumber;
        private String name;
        private String notes;
        private List<PlannedExercise> plannedExercises;
        private List<WorkoutSession> workoutSessions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlannedExercise {
        private Long id;
        private Long exerciseId;
        private Integer orderIndex;
        private Integer sets;
        private String repRange;
        private String restPeriod;
        private BigDecimal targetRpe;
        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkoutSession {
        private Long id;
        private Integer microcycleNumber;
        private LocalDate date;
        private String location;
        private String notes;
        private List<ExerciseLog> exerciseLogs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseLog {
        private Long id;
        private Long exerciseId;
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
}
