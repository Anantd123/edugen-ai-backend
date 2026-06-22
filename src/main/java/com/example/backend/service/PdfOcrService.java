package com.example.backend.service;

import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

@Service
@RequiredArgsConstructor
public class PdfOcrService {

    private final GeminiVisionService
            geminiVisionService;

    public String extractTextFromScannedPdf(
            File pdfFile)
            throws Exception {

        StringBuilder extractedText =
                new StringBuilder();

        PDDocument document =
                Loader.loadPDF(pdfFile);

        PDFRenderer renderer =
                new PDFRenderer(document);

        int totalPages =
                Math.min(
                        document.getNumberOfPages(),
                        10
                );

        for (int page = 0;
             page < totalPages;
             page++) {

            BufferedImage image =
                    renderer.renderImageWithDPI(
                            page,
                            150
                    );

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            ImageIO.write(
                    image,
                    "png",
                    baos
            );

            MockMultipartFile imageFile =
                    new MockMultipartFile(
                            "file",
                            "page-" + page + ".png",
                            "image/png",
                            baos.toByteArray()
                    );

            String pageText =
                    geminiVisionService
                            .extractTextFromImage(
                                    imageFile
                            );

            extractedText
                    .append(pageText)
                    .append("\n\n");

            System.out.println(
                    "Gemini Vision Completed Page "
                            + (page + 1)
            );
        }

        document.close();

        return extractedText.toString();
    }
}
