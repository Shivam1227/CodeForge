package com.judge.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.api.dto.SubmissionResultEvent;
import com.judge.api.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(SubmissionResultConsumer.class);

    private final ObjectMapper objectMapper;
    private final SubmissionService submissionService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "submission-results", groupId = "api-service-group")
    public void onMessage(String json) {
        try {
            SubmissionResultEvent event = objectMapper.readValue(json, SubmissionResultEvent.class);

            var updated = submissionService.updateStatus(
                            event.getSubmissionId(),
                            event.getStatus(),
                            event.getErrorMessage(),
                            event.getPassedCount(),
                            event.getTotalCount()
                    );
            messagingTemplate.convertAndSend(
                    "/topic/submissions/" + event.getSubmissionId(),
                    updated
            );
        } catch (Exception e) {
            log.error("Failed to process submission-result message: {}", json, e);
        }
    }
}