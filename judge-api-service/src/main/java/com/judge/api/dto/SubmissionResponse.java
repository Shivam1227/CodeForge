package com.judge.api.dto;

import com.judge.api.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmissionResponse {
    private Long submissionId;
    private SubmissionStatus status;
    private String errorMessage;
    private Integer passedCount;
    private Integer totalCount;
}