package com.example.converter_service.service;

import com.example.converter_service.converter.ConversionOutput;
import com.example.converter_service.converter.Converter;
import com.example.converter_service.converter.ConverterRegistry;
import com.example.converter_service.converter.ZipConverter;
import com.example.converter_service.dto.ConversionRequest;
import com.example.converter_service.exception.FileConversionException;
import com.example.converter_service.exception.StorageException;
import com.example.converter_service.exception.UnsupportedFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessor implements IFileProcessor {

    private final IMinioService minioService;
    private final ConverterRegistry converterRegistry;
    private final ZipConverter zipConverter;

    @Override
    public ConversionOutput process(ConversionRequest request) throws Exception {
        byte[] fileData;
        try {
            fileData = minioService.downloadFile(request.getSourceBucket(), request.getFilePath());
        } catch (Exception e) {
            throw new StorageException("Failed to download file: " + request.getFilePath(), e);
        }
        String fileName = request.getFilePath().substring(request.getFilePath().lastIndexOf('/') + 1);
        String fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();

        ConversionOutput output;
        try {
            if ("ZIP".equalsIgnoreCase(fileType)) {
                output = zipConverter.convert(fileData, fileName, converterRegistry);
            } else {
                Converter converter = converterRegistry.getConverter(fileType);
                if (converter == null) {
                    throw new UnsupportedFileTypeException(fileType);
                }
                output = converter.convert(fileData, fileName);
            }
        } catch (UnsupportedFileTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new FileConversionException("Conversion failed for " + fileName, e);
        }

        String outputPath = "converted/" + request.getRequestId() + "/" + output.getOutputFileName();
        try {
            minioService.uploadFile(request.getSourceBucket(), outputPath, output.getOutputData(), output.getContentType());
        } catch (Exception e) {
            throw new StorageException("Failed to upload converted file: " + outputPath, e);
        }

        return output;
    }
}