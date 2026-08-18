package com.judge.api.service;

import com.judge.api.dto.SubmissionEvent;
import com.judge.api.dto.SubmissionRequest;
import com.judge.api.dto.SubmissionResponse;
import com.judge.api.entity.Submission;
import com.judge.api.entity.SubmissionStatus;
import com.judge.api.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionProducer submissionProducer;

    public SubmissionResponse submit(Long userId, SubmissionRequest request) {
        Submission submission = Submission.builder()
                .userId(userId)
                .problemId(request.getProblemId())
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .status(SubmissionStatus.PENDING)
                .build();

        submission = submissionRepository.save(submission);

        submissionProducer.publish(new SubmissionEvent(
                submission.getId(),
                submission.getProblemId(),
                submission.getLanguage(),
                submission.getSourceCode()
        ));

        return new SubmissionResponse(
                submission.getId(),
                submission.getStatus(),
                null,
                null,
                null
        );
    }

    public SubmissionResponse getStatus(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        return new SubmissionResponse(
                submission.getId(),
                submission.getStatus(),
                submission.getErrorMessage(),
                submission.getPassedCount(),
                submission.getTotalCount()
        );
    }

    public SubmissionResponse updateStatus(
            Long submissionId,
            String statusName,
            String errorMessage,
            Integer passedCount,
            Integer totalCount
    ) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        SubmissionStatus status = SubmissionStatus.valueOf(statusName);

        submission.setStatus(status);
        submission.setErrorMessage(errorMessage);
        submission.setPassedCount(passedCount);
        submission.setTotalCount(totalCount);

        if (status != SubmissionStatus.PENDING && status != SubmissionStatus.RUNNING) {
            submission.setJudgedAt(Instant.now());
        }

        submissionRepository.save(submission);

        return new SubmissionResponse(
                submission.getId(),
                submission.getStatus(),
                submission.getErrorMessage(),
                submission.getPassedCount(),
                submission.getTotalCount()
        );
    }
}