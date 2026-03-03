package com.bodoczky.fittracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyCycleRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private String notes;
}
