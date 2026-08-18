package com.judge.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmissionHistoryItem {
    private Long submissionId;
    private Long problemId;
    private String problemTitle;
    private String language;
    private String status;
    private Integer passedCount;
    private Integer totalCount;
    private String createdAt; // ISO string
}