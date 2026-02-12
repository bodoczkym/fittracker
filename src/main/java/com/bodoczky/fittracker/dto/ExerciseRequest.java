package com.bodoczky.fittracker.dto;

import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseRequest {

    @NotBlank(message = "Exercise name is required")
    private String name;

    @NotNull(message = "Category is required")
    private ExerciseCategory category;

    private String description;
}
