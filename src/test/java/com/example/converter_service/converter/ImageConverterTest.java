package com.example.converter_service.converter;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ImageConverterTest {

    private final ImageConverter converter = new ImageConverter();

    @Test
    void shouldConvertPngToPdf() throws Exception {
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] input = baos.toByteArray();

        ConversionOutput output = converter.convert(input, "test.png");

        assertThat(output.getOutputFileName()).isEqualTo("test.pdf");
        assertThat(output.getContentType()).isEqualTo("application/pdf");
        assertThat(output.getOutputData()).isNotEmpty();
        assertThat(new String(output.getOutputData(), 0, 4)).startsWith("%PDF");
    }

    @Test
    void shouldSupportPngJpgJpeg() {
        assertThat(converter.supports("png")).isTrue();
        assertThat(converter.supports("jpg")).isTrue();
        assertThat(converter.supports("jpeg")).isTrue();
        assertThat(converter.supports("gif")).isFalse();
    }
}