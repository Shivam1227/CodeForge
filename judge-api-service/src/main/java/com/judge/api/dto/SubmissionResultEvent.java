package com.judge.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubmissionResultEvent {
    private Long submissionId;
    private String status;
    private String errorMessage;
    private Integer passedCount;
    private Integer totalCount;
}