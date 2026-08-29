package com.example.converter_service.converter;

public interface Converter {
    boolean supports(String fileType);
    ConversionOutput convert(byte[] input, String fileName) throws Exception;
}