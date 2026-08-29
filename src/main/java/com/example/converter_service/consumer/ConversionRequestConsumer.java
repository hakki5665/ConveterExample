package com.example.converter_service.consumer;

import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.service.ConversionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConversionRequestConsumer {

    private final ConversionService conversionService;
    private final ObjectMapper objectMapper;

    public ConversionRequestConsumer(ConversionService conversionService, ObjectMapper objectMapper) {
        this.conversionService = conversionService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "file-conversion-requests", containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            ConversionRequest request = objectMapper.readValue(record.value(), ConversionRequest.class);
            conversionService.processRequest(request);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
        }
    }
}