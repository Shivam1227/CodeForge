package com.judge.worker.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.worker.dto.RunEvent;
import com.judge.worker.service.RunService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RunConsumer {

    private static final Logger log = LoggerFactory.getLogger(RunConsumer.class);
    private final ObjectMapper objectMapper;
    private final RunService runService;

    @KafkaListener(topics = "runs", groupId = "judge-workers")
    public void onMessage(String json) {
        try {
            RunEvent event = objectMapper.readValue(json, RunEvent.class);
            runService.run(event);
        } catch (Exception e) {
            log.error("Failed to process run message: {}", json, e);
        }
    }
}