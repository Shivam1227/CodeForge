package com.judge.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProblemRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String difficulty; // EASY / MEDIUM / HARD

    @NotNull
    @Positive
    private Integer timeLimitMs;

    @NotNull
    @Positive
    private Integer memoryLimitMb;
}