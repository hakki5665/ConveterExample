package com.example.converter_service.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Set;

@Component
public class ImageConverter implements Converter {

    private static final Set<String> SUPPORTED = Set.of("PNG", "JPG", "JPEG");

    @Override
    public boolean supports(String fileType) {
        return SUPPORTED.contains(fileType.toUpperCase());
    }

    @Override
    public ConversionOutput convert(byte[] input, String fileName) throws Exception {
        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(input));
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float imgWidth = image.getWidth();
            float imgHeight = image.getHeight();
            float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight) * 0.9f;
            float scaledWidth = imgWidth * scale;
            float scaledHeight = imgHeight * scale;
            float x = (pageWidth - scaledWidth) / 2;
            float y = (pageHeight - scaledHeight) / 2;

            var pdImage = LosslessFactory.createFromImage(document, image);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            String newName = fileName.substring(0, fileName.lastIndexOf('.')) + ".pdf";
            return new ConversionOutput(baos.toByteArray(), newName, "application/pdf");
        }
    }
}