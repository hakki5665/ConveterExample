package com.example.converter_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConversionRequest {
    @NotBlank
    private String requestId;
    @NotBlank
    private String filePath;
    private String sourceBucket = "conversions";


}