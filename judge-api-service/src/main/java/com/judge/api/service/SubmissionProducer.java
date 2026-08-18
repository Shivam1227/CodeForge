package com.judge.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.api.dto.SubmissionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(SubmissionEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            // key = submissionId → all messages for the same submission stay ordered
            // on the same partition (not critical here since each submission is a
            // single message, but it's the correct default habit for Kafka producers)
            kafkaTemplate.send("submissions", event.getSubmissionId().toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish submission event", e);
        }
    }
}