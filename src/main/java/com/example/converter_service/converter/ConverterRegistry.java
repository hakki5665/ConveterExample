package com.example.converter_service.converter;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConverterRegistry {

    private final List<Converter> converters;

    public ConverterRegistry(List<Converter> converters) {
        this.converters = converters;
    }

    public Converter getConverter(String fileType) {
        return converters.stream()
                .filter(c -> c.supports(fileType.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}