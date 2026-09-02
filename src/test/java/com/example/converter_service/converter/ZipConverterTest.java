package com.example.converter_service.converter;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZipConverterTest {

    @Mock
    private ConverterRegistry converterRegistry;

    private ZipConverter zipConverter;

    @BeforeEach
    void setUp() {
        zipConverter = new ZipConverter();
    }

    @Test
    void shouldConvertZipWithTxtFile() throws Exception {
        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipBaos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
            zos.putArchiveEntry(entry);
            zos.write("Hello".getBytes());
            zos.closeArchiveEntry();
        }
        byte[] zipData = zipBaos.toByteArray();

        Converter txtConverter = new TxtConverter();
        when(converterRegistry.getConverter("TXT")).thenReturn(txtConverter);

        ConversionOutput output = zipConverter.convert(zipData, "archive.zip", converterRegistry);

        assertThat(output.getOutputFileName()).isEqualTo("archive.pdf.zip");
        assertThat(output.getContentType()).isEqualTo("application/zip");
        assertThat(output.getOutputData()).isNotEmpty();
    }
}