package com.judge.worker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
public class TestCase {

    @Id
    private Long id;

    @Column(name = "problem_id")
    private Long problemId;

    private String input;

    @Column(name = "expected_output")
    private String expectedOutput;

    @Column(name = "is_sample", nullable = false)
    private boolean isSample = false;
}