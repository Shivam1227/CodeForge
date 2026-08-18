package com.judge.api.controller;

import com.judge.api.dto.HeatmapEntry;
import com.judge.api.dto.ProfileResponse;
import com.judge.api.dto.SubmissionHistoryItem;
import com.judge.api.security.CustomUserDetails;
import com.judge.api.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.getProfile(userDetails.getId()));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<SubmissionHistoryItem>> getHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.getHistory(userDetails.getId()));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapEntry>> getHeatmap(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.getHeatmap(userDetails.getId()));
    }

    @GetMapping("/solved-problems")
    public ResponseEntity<List<Long>> getSolvedProblems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.getSolvedProblemIds(userDetails.getId()));
    }
}