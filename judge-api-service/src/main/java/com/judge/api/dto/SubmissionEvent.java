package com.judge.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionEvent {
    private Long submissionId;
    private Long problemId;
    private String language;
    private String sourceCode;
}