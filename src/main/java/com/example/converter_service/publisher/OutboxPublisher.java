package com.example.converter_service.publisher;

import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        List<OutboxRecord> pending = outboxRepository.findByStatus("PENDING");
        if (pending.isEmpty()) return;

        log.info("Publishing {} outbox records", pending.size());
        for (OutboxRecord record : pending) {
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(record.getTopic(), record.getMessageId(), record.getPayload());

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    record.setStatus("PUBLISHED");
                    record.setPublishedAt(LocalDateTime.now());
                    outboxRepository.save(record);
                    log.info("Published outbox record {}", record.getId());
                } else {
                    log.error("Failed to publish outbox record {}, will retry later", record.getId(), ex);
                }
            });
        }
    }
}