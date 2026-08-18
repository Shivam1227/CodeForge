package com.judge.api.service;

import com.judge.api.dto.ProblemResponse;
import com.judge.api.dto.SampleTestCaseDto;
import com.judge.api.entity.Problem;
import com.judge.api.entity.TestCase;
import com.judge.api.repository.ProblemRepository;
import com.judge.api.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    @Cacheable(value = "problems", key = "#id")
    public ProblemResponse getProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        List<SampleTestCaseDto> samples = testCaseRepository.findByProblemId(id).stream()
                .filter(TestCase::isSample)
                .map(tc -> new SampleTestCaseDto(
                        tc.getInput(),
                        tc.getExpectedOutput()
                ))
                .toList();

        return toResponse(problem, samples);
    }

    public List<ProblemResponse> getAllProblems() {
        return problemRepository.findAll().stream()
                .map(problem -> toResponse(problem, List.of()))
                .toList();
    }

    private ProblemResponse toResponse(
            Problem problem,
            List<SampleTestCaseDto> samples
    ) {
        return new ProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                samples
        );
    }
}