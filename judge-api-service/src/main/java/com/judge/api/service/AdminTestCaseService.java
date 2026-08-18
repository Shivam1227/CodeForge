package com.judge.api.service;

import com.judge.api.dto.TestCaseRequest;
import com.judge.api.dto.TestCaseResponse;
import com.judge.api.entity.TestCase;
import com.judge.api.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final CacheManager cacheManager;

    public List<TestCaseResponse> listForProblem(Long problemId) {
        return testCaseRepository.findByProblemId(problemId).stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(value = "problems", key = "#problemId")
    public TestCaseResponse add(Long problemId, TestCaseRequest request) {
        TestCase testCase = TestCase.builder()
                .problemId(problemId)
                .input(request.getInput())
                .expectedOutput(request.getExpectedOutput())
                .isSample(request.isSample())
                .build();
        return toResponse(testCaseRepository.save(testCase));
    }

    public TestCaseResponse update(Long id, TestCaseRequest request) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test case not found"));

        testCase.setInput(request.getInput());
        testCase.setExpectedOutput(request.getExpectedOutput());
        testCase.setSample(request.isSample());

        TestCaseResponse response = toResponse(testCaseRepository.save(testCase));
        evictProblemCache(testCase.getProblemId());
        return response;
    }

    public void delete(Long id) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Test case not found"));

        Long problemId = testCase.getProblemId(); // must read this BEFORE deleting the row
        testCaseRepository.deleteById(id);
        evictProblemCache(problemId);
    }

    private void evictProblemCache(Long problemId) {
        Cache cache = cacheManager.getCache("problems");
        if (cache != null) {
            cache.evict(problemId);
        }
    }

    private TestCaseResponse toResponse(TestCase tc) {
        return new TestCaseResponse(tc.getId(), tc.getInput(), tc.getExpectedOutput(), tc.isSample());
    }
}