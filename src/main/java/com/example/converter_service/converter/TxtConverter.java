package com.example.converter_service.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TxtConverter implements Converter {

    private static final int FONT_SIZE = 12;
    private static final float LINE_HEIGHT = 15f;
    private static final float MARGIN = 50f;

    @Override
    public boolean supports(String fileType) {
        return "TXT".equalsIgnoreCase(fileType);
    }

    @Override
    public ConversionOutput convert(byte[] input, String fileName) throws Exception {
        String text = new String(input, StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        try (PDDocument document = new PDDocument()) {
            String[] lines = text.split("\n", -1);
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, PDRectangle.A4.getHeight() - MARGIN);

            float y = PDRectangle.A4.getHeight() - MARGIN;
            float maxWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;

            for (String line : lines) {
                String[] words = line.split(" ", -1);
                StringBuilder currentLine = new StringBuilder();

                for (String word : words) {
                    String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                    float width = getStringWidth(testLine, FONT_SIZE);
                    if (width <= maxWidth) {
                        if (currentLine.length() > 0) currentLine.append(' ');
                        currentLine.append(word);
                    } else {
                        contentStream.showText(currentLine.toString());
                        y -= LINE_HEIGHT;
                        contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                        if (y < MARGIN) {
                            contentStream.endText();
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(MARGIN, PDRectangle.A4.getHeight() - MARGIN);
                            y = PDRectangle.A4.getHeight() - MARGIN;
                        }
                        currentLine = new StringBuilder(word);
                    }
                }

                if (currentLine.length() > 0) {
                    contentStream.showText(currentLine.toString());
                    y -= LINE_HEIGHT;
                    contentStream.newLineAtOffset(0, -LINE_HEIGHT);
                    if (y < MARGIN) {
                        contentStream.endText();
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(MARGIN, PDRectangle.A4.getHeight() - MARGIN);
                        y = PDRectangle.A4.getHeight() - MARGIN;
                    }
                }
            }

            contentStream.endText();
            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            String newName = fileName.substring(0, fileName.lastIndexOf('.')) + ".pdf";
            return new ConversionOutput(baos.toByteArray(), newName, "application/pdf");
        }
    }

    private float getStringWidth(String text, int fontSize) {
        try {
            return PDType1Font.HELVETICA.getStringWidth(text) / 1000f * fontSize;
        } catch (Exception e) {
            return text.length() * fontSize * 0.6f;
        }
    }
}