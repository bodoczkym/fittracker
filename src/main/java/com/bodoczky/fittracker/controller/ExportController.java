package com.bodoczky.fittracker.controller;

import com.bodoczky.fittracker.dto.ExportResponse;
import com.bodoczky.fittracker.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Full-database JSON backup endpoint (issue #16). Gated by HTTP Basic auth via the global
 * {@code anyRequest().authenticated()} rule (ADR-0001) — no per-endpoint config needed.
 */
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping
    public ResponseEntity<ExportResponse> export() {
        return ResponseEntity.ok(exportService.exportAll());
    }
}
