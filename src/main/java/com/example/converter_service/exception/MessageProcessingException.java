package com.example.converter_service.exception;

public class MessageProcessingException extends BusinessException {
    public MessageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}