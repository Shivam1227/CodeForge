package com.judge.api.service;

import com.judge.api.dto.ProblemRequest;
import com.judge.api.entity.Problem;
import com.judge.api.repository.ProblemRepository;
import com.judge.api.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public Problem create(ProblemRequest request) {
        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .timeLimitMs(request.getTimeLimitMs())
                .memoryLimitMb(request.getMemoryLimitMb())
                .build();
        return problemRepository.save(problem);
    }

    // Evicts the Phase 5 cache entry for this problem — without this, an edited
    // problem would keep serving stale cached data for up to 10 minutes, exactly
    // the gap flagged back in the Phase 5 summary.
    @CacheEvict(value = "problems", key = "#id")
    public Problem update(Long id, ProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setTimeLimitMs(request.getTimeLimitMs());
        problem.setMemoryLimitMb(request.getMemoryLimitMb());

        return problemRepository.save(problem);
    }

    @CacheEvict(value = "problems", key = "#id")
    public void delete(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new IllegalArgumentException("Problem not found");
        }
        // No JPA relation/FK is defined between TestCase and Problem (Phase 1
        // kept them as plain Long problemId columns), so test cases must be
        // deleted explicitly here — there's no cascade to rely on.
        testCaseRepository.deleteAll(testCaseRepository.findByProblemId(id));
        problemRepository.deleteById(id);
        // Note: existing Submissions referencing this problem are intentionally
        // left as historical records, not deleted.
    }
}