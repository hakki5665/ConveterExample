package com.example.converter_service.converter;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class ZipConverter implements Converter {

    private static final String OUTPUT_ZIP_CONTENT_TYPE = "application/zip";
    private static final String OUTPUT_ZIP_SUFFIX = ".pdf.zip";

    private final ConverterRegistry converterRegistry;

    public ZipConverter(@Lazy ConverterRegistry converterRegistry) {
        this.converterRegistry = converterRegistry;
    }

    @Override
    public boolean supports(String fileType) {
        return "ZIP".equalsIgnoreCase(fileType);
    }

    @Override
    public ConversionOutput convert(byte[] input, String fileName) throws Exception {
        ByteArrayOutputStream outputBaos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(outputBaos);
             ZipArchiveInputStream zipIn = new ZipArchiveInputStream(new java.io.ByteArrayInputStream(input))) {

            ZipArchiveEntry entry;
            while ((entry = zipIn.getNextZipEntry()) != null) {
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    byte[] fileData = IOUtils.toByteArray(zipIn);
                    String fileType = entryName.substring(entryName.lastIndexOf('.') + 1).toUpperCase();
                    Converter converter = converterRegistry.getConverter(fileType);

                    ZipArchiveEntry newEntry;
                    if (converter != null) {
                        ConversionOutput converted = converter.convert(fileData, entryName);
                        newEntry = new ZipArchiveEntry(
                                entryName.substring(0, entryName.lastIndexOf('.')) + ".pdf"
                        );
                        zipOut.putArchiveEntry(newEntry);
                        zipOut.write(converted.getOutputData());
                    } else {
                        newEntry = new ZipArchiveEntry("unsupported_" + entryName);
                        zipOut.putArchiveEntry(newEntry);
                        zipOut.write(fileData);
                    }
                    zipOut.closeArchiveEntry();
                }
            }
        }

        String newName = fileName.substring(0, fileName.lastIndexOf('.')) + OUTPUT_ZIP_SUFFIX;
        return new ConversionOutput(outputBaos.toByteArray(), newName, OUTPUT_ZIP_CONTENT_TYPE);
    }
}