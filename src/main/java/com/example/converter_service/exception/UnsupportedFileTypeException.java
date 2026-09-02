package com.example.converter_service.exception;

public class UnsupportedFileTypeException extends BusinessException {
    public UnsupportedFileTypeException(String fileType) {
        super("Unsupported file type: " + fileType);
    }
}