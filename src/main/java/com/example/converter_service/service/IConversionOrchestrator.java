package com.example.converter_service.service;

import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.dto.ConversionResult;

public interface IConversionOrchestrator {
    ConversionResult processRequest(ConversionRequest request) throws Exception;
}