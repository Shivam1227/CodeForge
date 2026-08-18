package com.judge.worker.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.judge.worker.dto.SubmissionEvent;
import com.judge.worker.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionConsumer {

    private static final Logger log = LoggerFactory.getLogger(SubmissionConsumer.class);

    private final ObjectMapper objectMapper;
    private final JudgeService judgeService;

    @KafkaListener(topics = "submissions", groupId = "judge-workers")
    public void onMessage(String json) {
        try {
            SubmissionEvent event = objectMapper.readValue(json, SubmissionEvent.class);
            judgeService.judge(event);
        } catch (Exception e) {
            // Deliberately caught here, not rethrown: an unhandled exception in a
            // @KafkaListener can stall the consumer on this message forever.
            // Phase 4 will add a proper dead-letter/retry strategy — for now, log and move on.
            log.error("Failed to process submission message: {}", json, e);
        }
    }
}