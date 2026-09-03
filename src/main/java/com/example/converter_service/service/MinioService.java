package com.example.converter_service.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class MinioService implements IMinioService {

    private final MinioClient minioClient;
    private final String defaultBucket;

    public MinioService(MinioClient minioClient,
                        @Value("${minio.bucket}") String defaultBucket) {
        this.minioClient = minioClient;
        this.defaultBucket = defaultBucket;
    }

    @PostConstruct
    public void init() throws Exception {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(defaultBucket).build()
        );
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(defaultBucket).build()
            );
        }
    }

    @Override
    public byte[] downloadFile(String bucket, String path) throws Exception {
        try (var stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(path).build()
        )) {
            return stream.readAllBytes();
        }
    }

    @Override
    public void uploadFile(String bucket, String path, byte[] data, String contentType) throws Exception {
        try (var inputStream = new ByteArrayInputStream(data)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(inputStream, data.length, -1)
                            .contentType(contentType)
                            .build()
            );
        }
    }
}