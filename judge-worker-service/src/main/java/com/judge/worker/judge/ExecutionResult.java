package com.judge.worker.judge;

public record ExecutionResult(String stdout, String stderr, int exitCode, boolean processTimedOut) {
}