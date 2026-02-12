package com.bodoczky.fittracker.dto;

import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResponse {

    private Long id;
    private String name;
    private ExerciseCategory category;
    private String description;
}
