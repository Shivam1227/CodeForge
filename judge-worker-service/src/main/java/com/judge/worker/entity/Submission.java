package com.judge.worker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "submissions")
@Getter
@Setter
public class Submission {

    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "problem_id")
    private Long problemId;

    private String language;

    @Column(name = "source_code")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "judged_at")
    private Instant judgedAt;
}