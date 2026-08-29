package com.example.converter_service.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class TxtConverter implements Converter {

    private static final int FONT_SIZE = 12;
    private static final float LINE_HEIGHT = 15f;
    private static final float MARGIN = 50f;
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight() - MARGIN * 2;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth() - MARGIN * 2;

    @Override
    public boolean supports(String fileType) {
        return "TXT".equalsIgnoreCase(fileType);
    }

    @Override
    public ConversionOutput convert(byte[] input, String fileName) throws Exception {
        String text = new String(input, java.nio.charset.StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        try (PDDocument document = new PDDocument()) {
            String[] lines = text.split("\n", -1);

            float y = PDRectangle.A4.getHeight() - MARGIN;
            PDPage page = null;
            PDPageContentStream contentStream = null;

            for (String line : lines) {
                if (page == null || y < MARGIN) {
                    if (contentStream != null) {
                        contentStream.close();
                    }
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
                    contentStream.newLineAtOffset(MARGIN, PDRectangle.A4.getHeight() - MARGIN);
                    y = PDRectangle.A4.getHeight() - MARGIN;
                }

                String trimmedLine = line;
                if (trimmedLine.isEmpty()) {
                    trimmedLine = " ";
                }

                while (!trimmedLine.isEmpty()) {
                    float stringWidth = getStringWidth(trimmedLine, FONT_SIZE);
                    if (stringWidth <= PAGE_WIDTH || trimmedLine.length() == 1) {
                        contentStream.showText(trimmedLine);
                        y -= LINE_HEIGHT;
                        contentStream.newLineAtOffset(0f, -LINE_HEIGHT);
                        trimmedLine = "";
                    } else {
                        int breakIndex = findBreakIndex(trimmedLine, PAGE_WIDTH, FONT_SIZE);
                        String part = trimmedLine.substring(0, breakIndex).trim();
                        contentStream.showText(part);
                        y -= LINE_HEIGHT;
                        contentStream.newLineAtOffset(0f, -LINE_HEIGHT);
                        trimmedLine = trimmedLine.substring(breakIndex).trim();
                    }

                    if (y < MARGIN) {
                        break;
                    }
                }

                if (!trimmedLine.isEmpty() && y < MARGIN) {
                    if (contentStream != null) {
                        contentStream.close();
                    }
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
                    contentStream.newLineAtOffset(MARGIN, PDRectangle.A4.getHeight() - MARGIN);
                    y = PDRectangle.A4.getHeight() - MARGIN;
                }
            }

            if (contentStream != null) {
                contentStream.endText();
                contentStream.close();
            }

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
            return text.length() * fontSize * 0.6f; // приблизительно
        }
    }

    private int findBreakIndex(String text, float maxWidth, int fontSize) {
        if (text.isEmpty()) return 0;
        int breakIndex = text.length();
        for (int i = text.length() - 1; i > 0; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                String candidate = text.substring(0, i);
                if (getStringWidth(candidate, fontSize) <= maxWidth) {
                    return i;
                }
            }
        }
        for (int i = text.length(); i > 0; i--) {
            if (getStringWidth(text.substring(0, i), fontSize) <= maxWidth) {
                return i;
            }
        }
        return 1;
    }
}