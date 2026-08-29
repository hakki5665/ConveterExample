package com.example.converter_service.dto;

import lombok.Data;

@Data
public class ConversionRequest {
    private String requestId;
    private String filePath;
    private String sourceBucket = "conversions";
}