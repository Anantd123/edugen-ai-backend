package com.example.backend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @PostConstruct
    public void testKey() {

        System.out.println(
                "API KEY LENGTH = "
                        + (apiKey == null ? 0 : apiKey.length())
        );
    }

    @PostConstruct
    public void verifyKey() {

        System.out.println("===============");
        System.out.println("API KEY = " + apiKey);
        System.out.println("LENGTH = " + apiKey.length());
        System.out.println("===============");
    }
    public String generateQuestions(
            String pdfContent,
            String questionTypes,
            String difficulty,
            Integer questionCount
    ) {

        if (pdfContent != null
                && pdfContent.length() > 60000) {

            pdfContent =
                    pdfContent.substring(
                            0,
                            60000
                    );

            System.out.println(
                    "Content Truncated To 60000 Characters"
            );
        }
        String prompt = """
You are an expert teacher.

Generate EXACTLY %d questions.

Selected Question Types:
%s

Difficulty Level:
%s

Return ONLY valid JSON.

JSON Format:

[
  {
    "type":"MCQ",
    "question":"Question Text",
    "options":[
      "Option A",
      "Option B",
      "Option C",
      "Option D"
    ]
  },
  {
    "type":"Short Answer",
    "question":"Question Text"
  },
  {
    "type":"Long Answer",
    "question":"Question Text"
  },
  {
    "type":"Assignment",
    "question":"Question Text"
  },
  {
    "type":"True / False",
    "question":"Question Text",
    "options":[
      "True",
      "False"
    ]
  }
]

Rules:

- Generate EXACTLY %d questions.
- Generate ONLY selected question types.
- Difficulty must match selected difficulty.
- Every question must be based on the provided content.
- Do not repeat questions.
- MCQ must contain exactly 4 options.
- True / False must contain exactly 2 options.
- Short Answer must not contain options.
- Long Answer must not contain options.
- Assignment must not contain options.
- Return only JSON.
- Do not return markdown.
- Do not return explanations.
- Do not return headings.

Content:

%s
"""
                .formatted(
                        questionCount,
                        questionTypes,
                        difficulty,
                        questionCount,
                        pdfContent
                );

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                        + apiKey;

        Map<String, Object> requestBody =
                Map.of(
                        "contents",
                        List.of(
                                Map.of(
                                        "parts",
                                        List.of(
                                                Map.of(
                                                        "text",
                                                        prompt
                                                )
                                        )
                                )
                        )
                );

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        requestBody,
                        headers
                );

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

        try {

            List candidates =
                    (List) response.getBody()
                            .get("candidates");

            Map candidate =
                    (Map) candidates.get(0);

            Map content =
                    (Map) candidate.get("content");

            List parts =
                    (List) content.get("parts");

            Map part =
                    (Map) parts.get(0);

            String result =
                    part.get("text")
                            .toString();

            result = result
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return result;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Gemini Response Parsing Failed: "
                            + e.getMessage()
            );
        }


    }
}