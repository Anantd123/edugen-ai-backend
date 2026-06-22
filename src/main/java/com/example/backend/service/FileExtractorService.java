package com.example.backend.service;

import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class FileExtractorService {

    private final PdfOcrService pdfOcrService;

    private final GeminiVisionService geminiVisionService;

    public String extractText(
            MultipartFile file)
            throws Exception {

        String fileName =
                file.getOriginalFilename();

        if (fileName == null) {

            throw new RuntimeException(
                    "Invalid File"
            );
        }

        fileName =
                fileName.toLowerCase();

        if (fileName.endsWith(".pdf")) {

            return extractPdf(file);
        }

        if (fileName.endsWith(".docx")) {

            return extractDocx(file);
        }

        if (fileName.endsWith(".pptx")) {

            return extractPptx(file);
        }

        if (fileName.endsWith(".txt")) {

            return extractTxt(file);
        }

        if (fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")) {

            return extractImage(file);
        }

        throw new RuntimeException(
                "Unsupported File Type"
        );
    }

    private String extractPdf(
            MultipartFile file)
            throws Exception {

        PDDocument document =
                Loader.loadPDF(
                        file.getBytes()
                );

        PDFTextStripper stripper =
                new PDFTextStripper();

        String text =
                stripper.getText(
                        document
                );

        document.close();

        System.out.println(
                "PDF Text Length = "
                        + text.length()
        );

        if (text != null
                && text.trim().length() > 100) {

            System.out.println(
                    "Normal PDF Detected"
            );

            return text;
        }

        System.out.println(
                "Scanned PDF Detected"
        );

        File tempPdf =
                File.createTempFile(
                        "scan",
                        ".pdf"
                );

        file.transferTo(
                tempPdf
        );

        String ocrText =
                pdfOcrService
                        .extractTextFromScannedPdf(
                                tempPdf
                        );

        tempPdf.delete();

        return ocrText;
    }

    private String extractImage(
            MultipartFile file)
            throws Exception {

        System.out.println(
                "Using Gemini Vision..."
        );

        String extractedText =
                geminiVisionService
                        .extractTextFromImage(
                                file
                        );

        System.out.println(
                "Vision Text Length = "
                        + extractedText.length()
        );

        return extractedText;
    }

    private String extractDocx(
            MultipartFile file)
            throws Exception {

        XWPFDocument document =
                new XWPFDocument(
                        file.getInputStream()
                );

        StringBuilder builder =
                new StringBuilder();

        document.getParagraphs()
                .forEach(
                        p -> builder.append(
                                        p.getText()
                                )
                                .append("\n")
                );

        document.close();

        return builder.toString();
    }

    private String extractPptx(
            MultipartFile file)
            throws Exception {

        XMLSlideShow ppt =
                new XMLSlideShow(
                        file.getInputStream()
                );

        StringBuilder builder =
                new StringBuilder();

        for (XSLFSlide slide :
                ppt.getSlides()) {

            if (slide.getTitle() != null) {

                builder.append(
                        slide.getTitle()
                ).append("\n");
            }

            slide.getShapes()
                    .forEach(
                            shape ->
                                    builder.append(
                                                    shape.getShapeName()
                                            )
                                            .append("\n")
                    );
        }

        ppt.close();

        return builder.toString();
    }

    private String extractTxt(
            MultipartFile file)
            throws Exception {

        return new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );
    }
}
