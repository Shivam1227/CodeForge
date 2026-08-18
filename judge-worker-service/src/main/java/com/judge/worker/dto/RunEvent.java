package com.judge.worker.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class RunEvent {
    private String runId;
    private Long problemId;
    private String language;
    private String sourceCode;
}