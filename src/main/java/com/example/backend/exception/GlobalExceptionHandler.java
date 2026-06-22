package com.example.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            MaxUploadSizeExceededException.class
    )
    public ResponseEntity<?> handleUploadSize(
            MaxUploadSizeExceededException ex
    ) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        "File size exceeds allowed limit."
                )
        );
    }
}