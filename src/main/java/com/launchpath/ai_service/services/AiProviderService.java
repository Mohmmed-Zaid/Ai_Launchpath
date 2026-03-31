package com.launchpath.ai_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiProviderService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.groq.api-key}")
    private String groqApiKey;

    @Value("${ai.groq.base-url}")
    private String groqBaseUrl;

    @Value("${ai.groq.free.model}")
    private String groqFreeModel;

    @Value("${ai.groq.pro.model}")
    private String groqProModel;

    // ── Main call with retry ──────────────────────────────

    public String callWithRetry(AiProvider provider,
                                String systemPrompt,
                                String userPrompt) {
        log.info("Calling Groq - provider: {}", provider);

        try {
            String response = callGroq(provider, systemPrompt, userPrompt);
            validateJson(response);
            return response;

        } catch (Exception firstEx) {
            log.warn("First Groq attempt failed: {}. Waiting 3s before retry...",
                    firstEx.getMessage());

            // Wait 3 seconds before retry
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            try {
                String stricterPrompt = userPrompt +
                        "\n\nCRITICAL: Return ONLY valid JSON. " +
                        "No markdown, no code blocks, no explanation.";

                String retryResponse = callGroq(
                        provider, systemPrompt, stricterPrompt
                );
                validateJson(retryResponse);
                log.info("Retry succeeded");
                return retryResponse;

            } catch (Exception secondEx) {
                log.error("Both Groq attempts failed: {}",
                        secondEx.getMessage());
                throw new AiServiceException(
                        "AI analysis failed after retry. Please try again."
                );
            }
        }
    }

    // ── Groq call — OpenAI compatible format ──────────────

    @SuppressWarnings("unchecked")
    private String callGroq(AiProvider provider,
                            String systemPrompt,
                            String userPrompt) {

        // Select model based on provider
        String model = (provider == AiProvider.GROQ_PRO)
                ? groqProModel
                : groqFreeModel;

        log.debug("Calling Groq model: {}", model);

        // Groq uses exact OpenAI API format
        // json_object mode enforces JSON output
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                systemPrompt + " Always respond with valid JSON only."),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3,
                "max_tokens", 3000,
                "response_format", Map.of("type", "json_object")
        );

        Map response = webClientBuilder
                .baseUrl(groqBaseUrl)
                .build()
                .post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + groqApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractContent(response);
    }

    // ── Extract content from OpenAI-compatible response ───

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message =
                    (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse Groq response: " + e.getMessage()
            );
        }
    }

    // ── JSON validation ───────────────────────────────────

    private void validateJson(String response) {
        try {
            String clean = cleanJson(response);
            objectMapper.readTree(clean);
        } catch (Exception e) {
            throw new AiServiceException(
                    "Invalid JSON from Groq: " + e.getMessage()
            );
        }
    }

    public String cleanJson(String response) {
        if (response == null) return "{}";
        return response
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }
}