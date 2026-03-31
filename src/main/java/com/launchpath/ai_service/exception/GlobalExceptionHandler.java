package com.launchpath.ai_service.exception;

import com.launchpath.ai_service.dto.response.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>>
    handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> {
            String field = ((FieldError) e).getField();
            errors.put(field, e.getDefaultMessage());
        });
        return ResponseEntity.badRequest()
                .body(ApiResponseDTO.error(
                        "Validation failed: " + errors,
                        "VALIDATION_ERROR"
                ));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRateLimit(
            RateLimitExceededException ex) {
        log.warn("Rate limit: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponseDTO.error(ex.getMessage(), "RATE_LIMIT_EXCEEDED"));
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAiService(
            AiServiceException ex) {
        log.error("AI error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponseDTO.error(ex.getMessage(), "AI_SERVICE_ERROR"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(ex.getMessage(), "NOT_FOUND"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegal(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponseDTO.error(ex.getMessage(), "BAD_REQUEST"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(
                        "Something went wrong. Please try again.",
                        "INTERNAL_ERROR"
                ));
    }
}
