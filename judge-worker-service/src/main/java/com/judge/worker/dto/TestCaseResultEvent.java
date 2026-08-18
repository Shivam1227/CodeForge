package com.judge.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TestCaseResultEvent {
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String status; // PASSED, FAILED, TIME_LIMIT_EXCEEDED, MEMORY_LIMIT_EXCEEDED, RUNTIME_ERROR
}