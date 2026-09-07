package com.example.converter_service.service;

import com.example.converter_service.converter.ConversionOutput;
import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.dto.ConversionResult;
import com.example.converter_service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionOrchestrator implements IConversionOrchestrator {

    private final IInboxOutboxManager inboxOutboxManager;
    private final IFileProcessor fileProcessor;

    @Override
    @Transactional
    public ConversionResult processRequest(ConversionRequest request) throws Exception {
        String messageId = request.getRequestId();

        if (inboxOutboxManager.isAlreadyProcessed(messageId)) {
            log.info("Message {} already processed, returning cached result", messageId);
            return inboxOutboxManager.getExistingResult(messageId)
                    .orElseThrow(() -> new IllegalStateException("Inbox exists but outbox missing"));
        }

        inboxOutboxManager.markAsProcessing(messageId);

        try {
            ConversionOutput output = fileProcessor.process(request);
            ConversionResult result = new ConversionResult();
            result.setRequestId(messageId);
            result.setStatus("SUCCESS");
            result.setOutputPath("converted/" + messageId + "/" + output.getOutputFileName());

            inboxOutboxManager.saveOutbox(messageId, result);
            inboxOutboxManager.markAsCompleted(messageId);
            return result;

        } catch (BusinessException e) {
            log.error("Business error during conversion for {}: {}", messageId, e.getMessage());
            ConversionResult result = new ConversionResult();
            result.setRequestId(messageId);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());

            inboxOutboxManager.saveOutbox(messageId, result);
            inboxOutboxManager.markAsFailed(messageId);
            return result;
        } catch (Exception e) {
            log.error("Unexpected error during conversion for {}", messageId, e);
            ConversionResult result = new ConversionResult();
            result.setRequestId(messageId);
            result.setStatus("FAILED");
            result.setErrorMessage("Unexpected error: " + e.getMessage());

            inboxOutboxManager.saveOutbox(messageId, result);
            inboxOutboxManager.markAsFailed(messageId);
            return result;
        }
    }
}
