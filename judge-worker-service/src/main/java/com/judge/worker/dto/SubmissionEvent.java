package com.judge.worker.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class SubmissionEvent {
    private Long submissionId;
    private Long problemId;
    private String language;
    private String sourceCode;
}