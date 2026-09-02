package com.example.converter_service.publisher;

import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import com.example.converter_service.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPending() {
        List<OutboxRecord> pending = outboxRepository.findByStatus(OutboxStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }

        log.info("Publishing {} outbox records", pending.size());

        for (OutboxRecord record : pending) {
            try {
                kafkaTemplate.send(record.getTopic(), record.getMessageId(), record.getPayload())
                        .get(10, TimeUnit.SECONDS);

                record.setStatus(OutboxStatus.PUBLISHED);
                record.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(record);
                log.info("Published outbox record {}", record.getMessageId());

            } catch (Exception e) {
                log.error("Failed to publish message {} to topic {}", record.getMessageId(), record.getTopic(), e);
            }
        }
    }
}