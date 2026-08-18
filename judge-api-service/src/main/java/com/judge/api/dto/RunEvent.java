package com.judge.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RunEvent {
    private String runId;
    private Long problemId;
    private String language;
    private String sourceCode;
}