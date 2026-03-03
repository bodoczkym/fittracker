package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.CopyCycleRequest;
import com.bodoczky.fittracker.dto.TrainingCycleRequest;
import com.bodoczky.fittracker.dto.TrainingCycleResponse;
import com.bodoczky.fittracker.service.TrainingCycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cycles")
@RequiredArgsConstructor
public class TrainingCycleController {

    private final TrainingCycleService trainingCycleService;

    @GetMapping
    public ResponseEntity<List<TrainingCycleResponse>> getAllCycles() {
        return ResponseEntity.ok(trainingCycleService.getAllCycles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingCycleResponse> getCycleById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingCycleService.getCycleById(id));
    }

    @PostMapping
    public ResponseEntity<TrainingCycleResponse> createCycle(
            @Valid @RequestBody TrainingCycleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingCycleService.createCycle(request));
    }

    @PostMapping("/copy")
    public ResponseEntity<TrainingCycleResponse> copyFromPreviousCycle(
            @Valid @RequestBody CopyCycleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingCycleService.copyFromPreviousCycle(
                        request.getStartDate(), request.getNotes()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingCycleResponse> updateCycle(
            @PathVariable Long id,
            @Valid @RequestBody TrainingCycleRequest request) {
        return ResponseEntity.ok(trainingCycleService.updateCycle(id, request));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<TrainingCycleResponse> endCycle(@PathVariable Long id) {
        return ResponseEntity.ok(trainingCycleService.endCycle(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCycle(@PathVariable Long id) {
        trainingCycleService.deleteCycle(id);
        return ResponseEntity.noContent().build();
    }
}
