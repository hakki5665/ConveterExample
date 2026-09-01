package com.example.converter_service.service;

import com.example.converter_service.dto.ConversionResult;
import com.example.converter_service.model.InboxRecord;
import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import com.example.converter_service.repository.InboxRepository;
import com.example.converter_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxOutboxManager implements IInboxOutboxManager {

    private final InboxRepository inboxRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public boolean isAlreadyProcessed(String messageId) {
        return inboxRepository.existsById(messageId);
    }

    @Override
    @Transactional
    public void markAsProcessing(String messageId) {
        InboxRecord inbox = new InboxRecord();
        inbox.setMessageId(messageId);
        inbox.setStatus("PROCESSING");
        inboxRepository.save(inbox);
    }

    @Override
    @Transactional
    public void markAsCompleted(String messageId) {
        inboxRepository.updateStatus(messageId, "COMPLETED", LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markAsFailed(String messageId) {
        inboxRepository.updateStatus(messageId, "FAILED", LocalDateTime.now());
    }

    @Override
    @Transactional
    public void saveOutbox(String messageId, ConversionResult result) throws Exception {
        String payload = objectMapper.writeValueAsString(result);
        OutboxRecord outbox = new OutboxRecord();
        outbox.setMessageId(messageId);
        outbox.setPayload(payload);
        outbox.setTopic("file-conversion-results");
        outbox.setStatus(OutboxStatus.PENDING);
        outboxRepository.save(outbox);
    }

    @Override
    public Optional<ConversionResult> getExistingResult(String messageId) throws Exception {
        return outboxRepository.findByMessageId(messageId)
                .map(record -> {
                    try {
                        return objectMapper.readValue(record.getPayload(), ConversionResult.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}