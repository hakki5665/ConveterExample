package com.example.converter_service.service;

public interface IMinioService {
    byte[] downloadFile(String bucket, String path) throws Exception;
    void uploadFile(String bucket, String path, byte[] data, String contentType) throws Exception;
}