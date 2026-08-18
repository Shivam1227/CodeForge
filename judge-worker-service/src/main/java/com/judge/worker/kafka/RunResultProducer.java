package com.judge.worker.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.worker.dto.RunResultEvent;
import com.judge.worker.dto.TestCaseResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RunResultProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String runId, String overallStatus, String errorMessage, List<TestCaseResultEvent> results) {
        try {
            String json = objectMapper.writeValueAsString(
                    new RunResultEvent(runId, overallStatus, errorMessage, results));
            kafkaTemplate.send("run-results", runId, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish run result", e);
        }
    }
}