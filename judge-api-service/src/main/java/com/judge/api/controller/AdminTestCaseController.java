package com.judge.api.controller;

import com.judge.api.dto.TestCaseRequest;
import com.judge.api.dto.TestCaseResponse;
import com.judge.api.service.AdminTestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminTestCaseController {

    private final AdminTestCaseService adminTestCaseService;

    @GetMapping("/problems/{problemId}/testcases")
    public ResponseEntity<List<TestCaseResponse>> list(@PathVariable Long problemId) {
        return ResponseEntity.ok(adminTestCaseService.listForProblem(problemId));
    }

    @PostMapping("/problems/{problemId}/testcases")
    public ResponseEntity<TestCaseResponse> add(@PathVariable Long problemId, @Valid @RequestBody TestCaseRequest request) {
        return ResponseEntity.ok(adminTestCaseService.add(problemId, request));
    }

    @PutMapping("/testcases/{id}")
    public ResponseEntity<TestCaseResponse> update(@PathVariable Long id, @Valid @RequestBody TestCaseRequest request) {
        return ResponseEntity.ok(adminTestCaseService.update(id, request));
    }

    @DeleteMapping("/testcases/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminTestCaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}