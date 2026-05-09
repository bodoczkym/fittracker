package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.ExerciseLogRequest;
import com.bodoczky.fittracker.dto.ExerciseLogResponse;
import com.bodoczky.fittracker.service.WorkoutSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class ExerciseLogController {

    private final WorkoutSessionService workoutSessionService;

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseLogResponse> updateLog(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseLogRequest request) {
        return ResponseEntity.ok(workoutSessionService.updateLog(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        workoutSessionService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}
