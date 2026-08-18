package com.judge.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {
    private String username;
    private String email;
    private String role;
    private String memberSince; // ISO string, null if not backfilled
    private long solvedCount;
    private long totalSubmissions;
    private int accuracy;
}