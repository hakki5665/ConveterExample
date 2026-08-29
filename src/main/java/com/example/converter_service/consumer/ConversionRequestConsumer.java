package com.example.converter_service.consumer;

import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.service.IConversionOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConversionRequestConsumer {

    private final IConversionOrchestrator orchestrator;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "file-conversion-requests", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            ConversionRequest request = objectMapper.readValue(record.value(), ConversionRequest.class);
            if (request.getRequestId() == null || request.getRequestId().isBlank()) {
                throw new IllegalArgumentException("requestId is blank");
            }
            orchestrator.processRequest(request);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message, sending to DLQ", e);
            kafkaTemplate.send("file-conversion-requests-dlq", record.key(), record.value());
            ack.acknowledge();
        }
    }
}