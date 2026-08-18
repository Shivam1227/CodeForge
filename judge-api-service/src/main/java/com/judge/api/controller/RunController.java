package com.judge.api.controller;

import com.judge.api.dto.RunEvent;
import com.judge.api.dto.SubmissionRequest;
import com.judge.api.service.RunProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/run")
@RequiredArgsConstructor
public class RunController {

    private final RunProducer runProducer;

    @PostMapping("/{problemId}")
    public ResponseEntity<Map<String, String>> run(@PathVariable Long problemId, @Valid @RequestBody SubmissionRequest request) {
        String runId = UUID.randomUUID().toString();
        RunEvent event = new RunEvent();
        event.setRunId(runId);
        event.setProblemId(problemId);
        event.setLanguage(request.getLanguage());
        event.setSourceCode(request.getSourceCode());
        runProducer.publish(event);
        return ResponseEntity.ok(Map.of("runId", runId));
    }
}