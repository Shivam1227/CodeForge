package com.judge.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RunResultEvent {
    private String runId;
    private String overallStatus; // COMPLETED, COMPILE_ERROR, RUNTIME_ERROR (internal judge error)
    private String errorMessage;
    private List<TestCaseResultEvent> results;
}