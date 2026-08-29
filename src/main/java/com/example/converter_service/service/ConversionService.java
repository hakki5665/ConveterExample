package com.example.converter_service.service;

import com.example.converter_service.converter.ConversionOutput;
import com.example.converter_service.converter.Converter;
import com.example.converter_service.converter.ConverterRegistry;
import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.dto.ConversionResult;
import com.example.converter_service.model.InboxRecord;
import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.repository.InboxRepository;
import com.example.converter_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ConversionService {

    private final MinioService minioService;
    private final ConverterRegistry converterRegistry;
    private final InboxRepository inboxRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ConversionService(MinioService minioService,
                             ConverterRegistry converterRegistry,
                             InboxRepository inboxRepository,
                             OutboxRepository outboxRepository,
                             ObjectMapper objectMapper) {
        this.minioService = minioService;
        this.converterRegistry = converterRegistry;
        this.inboxRepository = inboxRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ConversionResult processRequest(ConversionRequest request) throws Exception {
        String messageId = request.getRequestId();

        if (inboxRepository.existsById(messageId)) {
            log.info("Message {} already processed, skipping", messageId);
            var existing = outboxRepository.findByMessageId(messageId);
            if (existing.isPresent()) {
                return objectMapper.readValue(existing.get().getPayload(), ConversionResult.class);
            } else {
                throw new IllegalStateException("Inbox exists but outbox missing");
            }
        }

        InboxRecord inbox = new InboxRecord();
        inbox.setMessageId(messageId);
        inbox.setStatus("PROCESSING");
        inboxRepository.save(inbox);

        try {
            byte[] fileData = minioService.downloadFile(request.getSourceBucket(), request.getFilePath());
            String fileName = request.getFilePath().substring(request.getFilePath().lastIndexOf('/') + 1);
            String fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();

            Converter converter = converterRegistry.getConverter(fileType);
            if (converter == null) {
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
            }

            ConversionOutput output = converter.convert(fileData, fileName);

            String outputPath = "converted/" + request.getRequestId() + "/" + output.getOutputFileName();
            minioService.uploadFile(request.getSourceBucket(), outputPath, output.getOutputData(), output.getContentType());

            ConversionResult result = new ConversionResult();
            result.setRequestId(messageId);
            result.setStatus("SUCCESS");
            result.setOutputPath(outputPath);

            saveOutbox(messageId, result);

            inboxRepository.updateStatus(messageId, "COMPLETED", LocalDateTime.now());

            return result;

        } catch (Exception e) {
            log.error("Conversion failed for {}", messageId, e);
            ConversionResult result = new ConversionResult();
            result.setRequestId(messageId);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());

            saveOutbox(messageId, result);
            inboxRepository.updateStatus(messageId, "FAILED", LocalDateTime.now());
            return result;
        }
    }

    private void saveOutbox(String messageId, ConversionResult result) throws Exception {
        String payload = objectMapper.writeValueAsString(result);
        OutboxRecord outbox = new OutboxRecord();
        outbox.setMessageId(messageId);
        outbox.setPayload(payload);
        outbox.setTopic("file-conversion-results");
        outbox.setStatus("PENDING");
        outboxRepository.save(outbox);
    }
}