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
    public ResponseEntity<List<QuestionDTO>>
    generateQuestions(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("questionTypes")
            String questionTypes,

            @RequestParam("difficulty")
            String difficulty,

            @RequestParam("questionCount")
            Integer questionCount

    ) throws Exception {

        System.out.println(
                "================================="
        );

        System.out.println(
                "File Name: "
                        + file.getOriginalFilename()
        );

        System.out.println(
                "File Size: "
                        + file.getSize()
        );

        System.out.println(
                "Starting Extraction..."
        );

        String extractedText =
                fileExtractorService.extractText(file);

        System.out.println(
                "Extracted Text Length = "
                        + extractedText.length()
        );

        System.out.println(
                "Sending To Gemini..."
        );

        String aiResponse =
                geminiService.generateQuestions(
                        extractedText,
                        questionTypes,
                        difficulty,
                        questionCount
                );

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

        System.out.println(
                "================================="
        );

        return ResponseEntity.ok(
                questions
        );
    }
}