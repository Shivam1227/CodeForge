package com.judge.api.service;

import com.judge.api.dto.HeatmapEntry;
import com.judge.api.dto.ProfileResponse;
import com.judge.api.dto.SubmissionHistoryItem;
import com.judge.api.entity.Problem;
import com.judge.api.entity.Submission;
import com.judge.api.entity.SubmissionStatus;
import com.judge.api.entity.User;
import com.judge.api.repository.ProblemRepository;
import com.judge.api.repository.SubmissionRepository;
import com.judge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        long solved = submissionRepository.countDistinctSolvedProblems(userId, SubmissionStatus.ACCEPTED);
        long total = submissionRepository.countByUserId(userId);
        long accepted = submissionRepository.countByUserIdAndStatus(userId, SubmissionStatus.ACCEPTED);
        int accuracy = total == 0 ? 0 : (int) Math.round((accepted * 100.0) / total);

        return new ProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                solved,
                total,
                accuracy
        );
    }

    public List<Long> getSolvedProblemIds(Long userId) {
        return submissionRepository.findDistinctSolvedProblemIds(userId, SubmissionStatus.ACCEPTED);
    }

    public List<SubmissionHistoryItem> getHistory(Long userId) {
        List<Submission> submissions = submissionRepository.findTop100ByUserIdOrderByCreatedAtDesc(userId);

        // Batch-fetch problem titles instead of one query per submission (N+1 avoidance) —
        // there's no JPA relation between Submission and Problem (Phase 1 kept plain Long
        // columns), so this lookup has to happen at the service layer.
        List<Long> problemIds = submissions.stream().map(Submission::getProblemId).distinct().toList();
        Map<Long, String> titleById = problemRepository.findAllById(problemIds).stream()
                .collect(Collectors.toMap(Problem::getId, Problem::getTitle));

        return submissions.stream()
                .map(s -> new SubmissionHistoryItem(
                        s.getId(),
                        s.getProblemId(),
                        titleById.getOrDefault(s.getProblemId(), "Unknown problem"),
                        s.getLanguage(),
                        s.getStatus().name(),
                        s.getPassedCount(),
                        s.getTotalCount(),
                        s.getCreatedAt().toString()
                ))
                .toList();
    }

    public List<HeatmapEntry> getHeatmap(Long userId) {
        return submissionRepository.heatmapRawData(userId).stream()
                .map(row -> new HeatmapEntry(row[0].toString(), ((Number) row[1]).longValue()))
                .toList();
    }
}