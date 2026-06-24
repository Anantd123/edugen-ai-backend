package com.example.backend.controller;

import com.example.backend.dto.QuestionDTO;
import com.example.backend.service.FileExtractorService;
import com.example.backend.service.GeminiService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuestionController {

    private final FileExtractorService fileExtractorService;

    private final GeminiService geminiService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateQuestions(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("questionTypes")
            String questionTypes,

            @RequestParam("difficulty")
            String difficulty,

            @RequestParam("questionCount")
            Integer questionCount

    ) {

        try {

            System.out.println("=================================");
            System.out.println("REQUEST RECEIVED");
            System.out.println("File Name = " + file.getOriginalFilename());
            System.out.println("File Size = " + file.getSize());

            String extractedText =
                    fileExtractorService.extractText(file);

            System.out.println(
                    "Extracted Text Length = "
                            + extractedText.length()
            );

            String aiResponse =
                    geminiService.generateQuestions(
                            extractedText,
                            questionTypes,
                            difficulty,
                            questionCount
                    );

            System.out.println("Gemini Response:");
            System.out.println(aiResponse);

            ObjectMapper mapper =
                    new ObjectMapper();

            List<QuestionDTO> questions =
                    mapper.readValue(
                            aiResponse,
                            new TypeReference<List<QuestionDTO>>() {
                            }
                    );

            System.out.println(
                    "Questions Generated = "
                            + questions.size()
            );

            return ResponseEntity.ok(
                    questions
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            "ERROR: "
                                    + e.getClass().getSimpleName()
                                    + " -> "
                                    + e.getMessage()
                    );
        }
    }
}