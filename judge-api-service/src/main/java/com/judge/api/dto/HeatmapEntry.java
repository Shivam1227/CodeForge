package com.judge.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HeatmapEntry {
    private String date; // yyyy-MM-dd
    private long count;
}