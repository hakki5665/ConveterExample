package com.example.converter_service.converter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TxtConverterTest {

    private final TxtConverter converter = new TxtConverter();

    @Test
    void shouldConvertTxtToPdf() throws Exception {
        String text = "Hello, World!";
        byte[] input = text.getBytes();

        ConversionOutput output = converter.convert(input, "test.txt");

        assertThat(output.getOutputFileName()).isEqualTo("test.pdf");
        assertThat(output.getContentType()).isEqualTo("application/pdf");
        assertThat(output.getOutputData()).isNotEmpty();
        assertThat(new String(output.getOutputData(), 0, 4)).startsWith("%PDF");
    }

    @Test
    void shouldSupportTxtOnly() {
        assertThat(converter.supports("txt")).isTrue();
        assertThat(converter.supports("TXT")).isTrue();
        assertThat(converter.supports("pdf")).isFalse();
    }
}