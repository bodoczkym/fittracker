package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.ExerciseLogRequest;
import com.bodoczky.fittracker.dto.ExerciseLogResponse;
import com.bodoczky.fittracker.dto.WorkoutSessionRequest;
import com.bodoczky.fittracker.dto.WorkoutSessionResponse;
import com.bodoczky.fittracker.service.WorkoutSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @GetMapping
    public ResponseEntity<List<WorkoutSessionResponse>> getSessions(
            @RequestParam Long cycleId,
            @RequestParam(required = false) Integer microcycleNumber) {
        if (microcycleNumber != null) {
            return ResponseEntity.ok(workoutSessionService.getSessionsByCycleAndMicrocycle(cycleId, microcycleNumber));
        }
        return ResponseEntity.ok(workoutSessionService.getSessionsByCycle(cycleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutSessionResponse> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutSessionService.getSessionById(id));
    }

    @PostMapping
    public ResponseEntity<WorkoutSessionResponse> createSession(
            @Valid @RequestBody WorkoutSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutSessionService.createSession(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutSessionResponse> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutSessionRequest request) {
        return ResponseEntity.ok(workoutSessionService.updateSession(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        workoutSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/logs")
    public ResponseEntity<ExerciseLogResponse> addLog(
            @PathVariable Long sessionId,
            @Valid @RequestBody ExerciseLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutSessionService.addLog(sessionId, request));
    }
}
