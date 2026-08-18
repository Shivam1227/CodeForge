package com.judge.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.api.dto.RunEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RunProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(RunEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("runs", event.getRunId(), json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish run event", e);
        }
    }
}