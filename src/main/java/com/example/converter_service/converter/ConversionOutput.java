package com.example.converter_service.converter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversionOutput {
    private byte[] outputData;
    private String outputFileName;
    private String contentType = "application/pdf";
}