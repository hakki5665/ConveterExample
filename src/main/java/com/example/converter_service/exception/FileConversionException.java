package com.example.converter_service.exception;

public class FileConversionException extends BusinessException {
    public FileConversionException(String message) {
        super(message);
    }
    public FileConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}