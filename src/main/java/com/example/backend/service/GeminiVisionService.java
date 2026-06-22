package com.example.backend.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiVisionService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    public String extractTextFromImage(
            MultipartFile file
    ) throws Exception {

        String mimeType =
                file.getContentType();

        String base64Image =
                Base64.getEncoder()
                        .encodeToString(
                                file.getBytes()
                        );

        String prompt = """
Read all educational content from this image.

Rules:

- Extract complete text.
- Fix OCR mistakes automatically.
- Preserve headings.
- Preserve bullet points.
- Preserve numbering.
- Return only extracted content.
- Do not explain anything.
- Do not summarize.
""";

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
                                                ),
                                                Map.of(
                                                        "inline_data",
                                                        Map.of(
                                                                "mime_type",
                                                                mimeType,
                                                                "data",
                                                                base64Image
                                                        )
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

            return part.get("text")
                    .toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Gemini Vision Extraction Failed: "
                            + e.getMessage()
            );
        }
    }
}

