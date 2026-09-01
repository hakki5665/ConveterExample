package com.example.converter_service.service;

import com.example.converter_service.BaseIntegrationTest;
import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.dto.ConversionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ConversionOrchestratorIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IConversionOrchestrator orchestrator;

    @Autowired
    private IMinioService minioService;

    @BeforeEach
    void setUp() throws Exception {
        String content = "Hello, World! This is a test file for conversion.";
        minioService.uploadFile("conversions", "test.txt", content.getBytes(), "text/plain");
    }

    @Test
    void shouldProcessRequestSuccessfully() throws Exception {
        String requestId = "test-123";
        ConversionRequest request = new ConversionRequest();
        request.setRequestId(requestId);
        request.setFilePath("test.txt");
        request.setSourceBucket("conversions");

        ConversionResult result = orchestrator.processRequest(request);

        assertThat(result).isNotNull();
        assertThat(result.getRequestId()).isEqualTo(requestId);
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getOutputPath()).contains(requestId);
        assertThat(result.getErrorMessage()).isNull();

        byte[] uploaded = minioService.downloadFile(
                "conversions",
                result.getOutputPath()
        );
        assertThat(uploaded).isNotEmpty();
        assertThat(new String(uploaded, 0, 4)).startsWith("%PDF");
    }

    @Test
    void shouldHandleUnsupportedFileType() throws Exception {
        String unsupportedContent = "Unsupported content";
        minioService.uploadFile("conversions", "unsupported.xyz", unsupportedContent.getBytes(), "application/octet-stream");

        ConversionRequest request = new ConversionRequest();
        request.setRequestId("test-456");
        request.setFilePath("unsupported.xyz");
        request.setSourceBucket("conversions");

        ConversionResult result = orchestrator.processRequest(request);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getErrorMessage()).contains("Unsupported file type");
    }

    @Test
    void shouldReturnCachedResultForDuplicateMessage() throws Exception {
        String requestId = "duplicate-123";
        ConversionRequest request = new ConversionRequest();
        request.setRequestId(requestId);
        request.setFilePath("test.txt");
        request.setSourceBucket("conversions");

        ConversionResult firstResult = orchestrator.processRequest(request);

        ConversionResult secondResult = orchestrator.processRequest(request);

        assertThat(secondResult.getStatus()).isEqualTo("SUCCESS");
        assertThat(secondResult.getOutputPath()).isEqualTo(firstResult.getOutputPath());
    }
}