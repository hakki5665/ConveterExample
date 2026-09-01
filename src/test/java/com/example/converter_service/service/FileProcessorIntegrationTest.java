package com.example.converter_service.service;

import com.example.converter_service.BaseIntegrationTest;
import com.example.converter_service.converter.ConversionOutput;
import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.exception.UnsupportedFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileProcessorIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IFileProcessor fileProcessor;

    @Autowired
    private IMinioService minioService;

    @BeforeEach
    void setUp() throws Exception {
        String content = "Hello, World! This is a test file for conversion.";
        minioService.uploadFile("conversions", "test.txt", content.getBytes(), "text/plain");
    }

    @Test
    void shouldConvertTxtToPdf() throws Exception {
        ConversionRequest request = new ConversionRequest();
        request.setRequestId("test-123");
        request.setFilePath("test.txt");
        request.setSourceBucket("conversions");

        ConversionOutput output = fileProcessor.process(request);

        assertThat(output.getOutputFileName()).endsWith(".pdf");
        assertThat(output.getContentType()).isEqualTo("application/pdf");
        assertThat(output.getOutputData()).isNotEmpty();

        byte[] uploaded = minioService.downloadFile(
                "conversions",
                "converted/test-123/" + output.getOutputFileName()
        );
        assertThat(uploaded).isNotEmpty();
    }

    @Test
    void shouldThrowExceptionForUnsupportedFileType() throws Exception {
        String unsupportedContent = "Unsupported content";
        minioService.uploadFile("conversions", "test.xyz", unsupportedContent.getBytes(), "application/octet-stream");

        ConversionRequest request = new ConversionRequest();
        request.setRequestId("test-456");
        request.setFilePath("test.xyz");
        request.setSourceBucket("conversions");

        assertThatThrownBy(() -> fileProcessor.process(request))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("Unsupported file type");
    }
}