package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.ExerciseRequest;
import com.bodoczky.fittracker.dto.ExerciseResponse;
import com.bodoczky.fittracker.model.enums.ExerciseCategory;
import com.bodoczky.fittracker.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getAllExercises() {
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getExerciseById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.getExerciseById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByCategory(
            @PathVariable ExerciseCategory category) {
        return ResponseEntity.ok(exerciseService.getExercisesByCategory(category));
    }

    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(exerciseService.createExercise(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.ok(exerciseService.updateExercise(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }
}
