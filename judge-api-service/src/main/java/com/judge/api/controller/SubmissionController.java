package com.judge.api.controller;

import com.judge.api.dto.SubmissionRequest;
import com.judge.api.dto.SubmissionResponse;
import com.judge.api.security.CustomUserDetails;
import com.judge.api.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse response = submissionService.submit(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubmissionResponse> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getStatus(id));
    }
}