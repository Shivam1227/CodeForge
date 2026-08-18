package com.judge.worker.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.worker.dto.SubmissionResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResultProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // ResultProducer.java — update publish() signature
    public void publish(Long submissionId, String status, String errorMessage, Integer passedCount, Integer totalCount) {
        try {
            String json = objectMapper.writeValueAsString(
                    new SubmissionResultEvent(submissionId, status, errorMessage, passedCount, totalCount));
            kafkaTemplate.send("submission-results", submissionId.toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish result event", e);
        }
    }
}