package com.judge.api.controller;

import com.judge.api.dto.ProblemRequest;
import com.judge.api.entity.Problem;
import com.judge.api.service.AdminProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/problems")
@RequiredArgsConstructor
public class AdminProblemController {

    private final AdminProblemService adminProblemService;

    @PostMapping
    public ResponseEntity<Problem> create(@Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(adminProblemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Problem> update(@PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(adminProblemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminProblemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}