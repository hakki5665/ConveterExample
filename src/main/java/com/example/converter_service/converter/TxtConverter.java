package com.example.converter_service.converter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TxtConverter implements Converter {

    private static final String INPUT_TYPE = "TXT";
    private static final String OUTPUT_SUFFIX = ".pdf";
    private static final String CONTENT_TYPE = "application/pdf";

    private static final PDType1Font FONT = PDType1Font.HELVETICA;
    private static final float FONT_SIZE = 12;
    private static final float LINE_HEIGHT = 15;
    private static final float MARGIN = 50;

    @Override
    public boolean supports(String fileType) {
        return INPUT_TYPE.equalsIgnoreCase(fileType);
    }

    @Override
    public ConversionOutput convert(byte[] input, String fileName) throws Exception {
        String text = new String(input, StandardCharsets.UTF_8);

        try (PDDocument document = new PDDocument()) {
            PDPage page = createNewPage(document);
            PDPageContentStream contentStream = createContentStream(document, page);

            float currentY = page.getMediaBox().getHeight() - MARGIN;
            float maxLineWidth = page.getMediaBox().getWidth() - (2 * MARGIN);

            List<String> lines = splitTextIntoLines(text, maxLineWidth);

            for (String line : lines) {
                if (currentY < MARGIN + LINE_HEIGHT) {
                    contentStream.close();
                    page = createNewPage(document);
                    contentStream = createContentStream(document, page);
                    currentY = page.getMediaBox().getHeight() - MARGIN;
                }

                writeLine(contentStream, line, currentY);
                currentY -= LINE_HEIGHT;
            }

            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);

            String outputFileName = fileName.substring(0, fileName.lastIndexOf('.')) + OUTPUT_SUFFIX;
            return new ConversionOutput(baos.toByteArray(), outputFileName, CONTENT_TYPE);
        }
    }

    private PDPage createNewPage(PDDocument document) {
        PDPage page = new PDPage();
        document.addPage(page);
        return page;
    }

    private PDPageContentStream createContentStream(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream stream = new PDPageContentStream(document, page);
        stream.setFont(FONT, FONT_SIZE);
        return stream;
    }

    private void writeLine(PDPageContentStream stream, String line, float y) throws IOException {
        stream.beginText();
        stream.newLineAtOffset(MARGIN, y);
        stream.showText(line);
        stream.endText();
    }


    private List<String> splitTextIntoLines(String text, float maxLineWidth) throws IOException {
        List<String> resultLines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float lineWidth = FONT.getStringWidth(testLine) / 1000 * FONT_SIZE;

            if (lineWidth > maxLineWidth) {
                resultLines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (!currentLine.isEmpty()) {
            resultLines.add(currentLine.toString());
        }

        return resultLines;
    }
}