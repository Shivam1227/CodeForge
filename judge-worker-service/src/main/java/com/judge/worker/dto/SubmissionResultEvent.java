package com.judge.worker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResultEvent {
    private Long submissionId;
    private String status;
    private String errorMessage; // null unless status is COMPILE_ERROR or RUNTIME_ERROR
    private Integer passedCount;
    private Integer totalCount;
}