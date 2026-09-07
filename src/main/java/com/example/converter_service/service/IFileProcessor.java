package com.example.converter_service.service;

import com.example.converter_service.converter.ConversionOutput;
import com.example.converter_service.dto.ConversionRequest;

public interface IFileProcessor {
    ConversionOutput process(ConversionRequest request) throws Exception;
}