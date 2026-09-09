package com.example.converter_service.dto;

import lombok.Data;

@Data
public class ConversionResult {
    private String requestId;
    private String status;
    private String outputPath;
    private String errorMessage;
}