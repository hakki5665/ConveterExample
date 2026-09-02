package com.example.converter_service.repository;

import com.example.converter_service.BaseIntegrationTest;
import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void shouldFindByStatus() {
        OutboxRecord record = new OutboxRecord();
        record.setMessageId(UUID.randomUUID().toString());
        record.setPayload("{}");
        record.setTopic("test");
        record.setStatus(OutboxStatus.PENDING);
        outboxRepository.save(record);

        List<OutboxRecord> found = outboxRepository.findByStatus(OutboxStatus.PENDING);
        assertThat(found).isNotEmpty();
        assertThat(found).anyMatch(r -> r.getMessageId().equals(record.getMessageId()));
    }

    @Test
    void shouldFindByMessageId() {
        String id = UUID.randomUUID().toString();
        OutboxRecord record = new OutboxRecord();
        record.setMessageId(id);
        record.setPayload("{}");
        record.setTopic("test");
        record.setStatus(OutboxStatus.PENDING);
        outboxRepository.save(record);

        OutboxRecord found = outboxRepository.findByMessageId(id).orElseThrow();
        assertThat(found.getMessageId()).isEqualTo(id);
    }
}