package com.judge.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.api.dto.RunResultEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RunResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(RunResultConsumer.class);
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "run-results", groupId = "api-service-group")
    public void onMessage(String json) {
        try {
            RunResultEvent event = objectMapper.readValue(json, RunResultEvent.class);
            messagingTemplate.convertAndSend("/topic/runs/" + event.getRunId(), event);
        } catch (Exception e) {
            log.error("Failed to process run-result message: {}", json, e);
        }
    }
}