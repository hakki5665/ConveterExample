package com.example.converter_service.repository;

import com.example.converter_service.BaseIntegrationTest;
import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void shouldFindPendingRecordsOrderedByCreatedAtAsc() {
        outboxRepository.deleteAll();

        OutboxRecord record1 = createRecord(UUID.randomUUID().toString(), OutboxStatus.PENDING, LocalDateTime.now().minusMinutes(5));
        OutboxRecord record2 = createRecord(UUID.randomUUID().toString(), OutboxStatus.PENDING, LocalDateTime.now().minusMinutes(10));
        OutboxRecord record3 = createRecord(UUID.randomUUID().toString(), OutboxStatus.PUBLISHED, LocalDateTime.now().minusMinutes(15));

        outboxRepository.saveAll(List.of(record1, record2, record3));

        List<OutboxRecord> result = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessageId()).isEqualTo(record2.getMessageId());
        assertThat(result.get(1).getMessageId()).isEqualTo(record1.getMessageId());
    }

    private OutboxRecord createRecord(String messageId, OutboxStatus status, LocalDateTime createdAt) {
        OutboxRecord record = new OutboxRecord();
        record.setMessageId(messageId);
        record.setPayload("{}");
        record.setTopic("test-topic");
        record.setStatus(status);
        record.setCreatedAt(createdAt);
        return record;
    }
}