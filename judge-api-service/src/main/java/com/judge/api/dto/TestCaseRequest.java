package com.judge.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseRequest {

    @NotBlank
    private String input;

    @NotBlank
    private String expectedOutput;

    private boolean isSample;
}