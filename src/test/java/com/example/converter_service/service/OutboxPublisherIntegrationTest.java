package com.example.converter_service.service;

import com.example.converter_service.BaseIntegrationTest;
import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import com.example.converter_service.publisher.OutboxPublisher;
import com.example.converter_service.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class OutboxPublisherIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void shouldPublishPendingOutboxRecords() {
        String messageId = UUID.randomUUID().toString();
        OutboxRecord record = new OutboxRecord();
        record.setMessageId(messageId);
        record.setPayload("{\"status\":\"SUCCESS\"}");
        record.setTopic("file-conversion-results");
        record.setStatus(OutboxStatus.PENDING);
        record.setCreatedAt(LocalDateTime.now());
        outboxRepository.save(record);

        outboxPublisher.publishPending();

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OutboxRecord updated = outboxRepository.findByMessageId(messageId).orElseThrow();
                    assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
                    assertThat(updated.getPublishedAt()).isNotNull();
                });
    }

    @Test
    void shouldRetryFailedPublish() {
        String messageId = UUID.randomUUID().toString();
        OutboxRecord record = new OutboxRecord();
        record.setMessageId(messageId);
        record.setPayload("{\"status\":\"SUCCESS\"}");

        record.setTopic("invalid/topic/name/causing/immediate/failure");

        record.setStatus(OutboxStatus.PENDING);
        record.setCreatedAt(LocalDateTime.now());
        outboxRepository.save(record);

        outboxPublisher.publishPending();

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    OutboxRecord updated = outboxRepository.findByMessageId(messageId).orElseThrow();
                    assertThat(updated.getStatus()).isEqualTo(OutboxStatus.PENDING);
                });
    }
}