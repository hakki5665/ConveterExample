package com.example.converter_service.service;

import com.example.converter_service.dto.ConversionResult;

import java.util.Optional;

public interface IInboxOutboxManager {
    boolean isAlreadyProcessed(String messageId);

    void markAsProcessing(String messageId);

    void markAsCompleted(String messageId);

    void markAsFailed(String messageId);

    void saveOutbox(String messageId, ConversionResult result) throws Exception;

    Optional<ConversionResult> getExistingResult(String messageId) throws Exception;
}
