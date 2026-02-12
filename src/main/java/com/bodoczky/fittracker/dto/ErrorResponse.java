package com.bodoczky.fittracker.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private int status;
    private String message;
    private List<String> errors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
