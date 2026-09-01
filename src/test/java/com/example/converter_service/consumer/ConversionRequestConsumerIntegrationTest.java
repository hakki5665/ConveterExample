package com.example.converter_service.consumer;

import com.example.converter_service.BaseIntegrationTest;
import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.model.OutboxRecord;
import com.example.converter_service.model.OutboxStatus;
import com.example.converter_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ConversionRequestConsumerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldProcessValidRequestAndSaveOutbox() throws Exception {
        String requestId = UUID.randomUUID().toString();
        ConversionRequest request = new ConversionRequest();
        request.setRequestId(requestId);
        request.setFilePath("test.txt");
        request.setSourceBucket("conversions");

        String json = objectMapper.writeValueAsString(request);
        sendKafkaMessage("file-conversion-requests", requestId, json);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<OutboxRecord> records = outboxRepository.findByStatus(OutboxStatus.PENDING);
                    assertThat(records).isNotEmpty();

                    OutboxRecord targetRecord = records.stream()
                            .filter(r -> requestId.equals(r.getMessageId()))
                            .findFirst()
                            .orElse(null);

                    assertThat(targetRecord).isNotNull();
                    assertThat(targetRecord.getMessageId()).isEqualTo(requestId);
                });
    }

    @Test
    void shouldSendToDlqWhenRequestIdIsMissing() throws Exception {
        String invalidJson = "{\"filePath\":\"test.txt\"}";
        sendKafkaMessage("file-conversion-requests", null, invalidJson);
    }

    private void sendKafkaMessage(String topic, String key, String value) {
        kafkaTemplate.send(new ProducerRecord<>(topic, key, value));
    }
}