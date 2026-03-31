// ══════════════════════════════════════════════════════════
// service/WhyRejectedService.java
// ══════════════════════════════════════════════════════════
package com.launchpath.ai_service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpath.ai_service.dto.request.WhyRejectedRequestDTO;
import com.launchpath.ai_service.dto.response.WhyRejectedResponseDTO;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhyRejectedService {

    private final AiProviderService aiProviderService;
    private final ObjectMapper objectMapper;

    public WhyRejectedResponseDTO analyzeRejection(
            WhyRejectedRequestDTO request,
            AiProvider provider) {

        log.info("Rejection analysis - userId: {}", request.getUserId());

        String systemPrompt =
                "You are a senior hiring manager and resume expert. " +
                        "Analyze why a resume would be rejected for a specific job. " +
                        "Respond with valid JSON only.";

        String userPrompt = """
            Analyze why this resume would be rejected and return JSON:
            {
              "missingSkills": ["skill1", "skill2"],
              "weakAreas": ["weak area 1", "weak area 2"],
              "improvements": ["specific improvement 1"],
              "bulletImprovements": [
                {
                  "original": "Worked on Java projects",
                  "improved": "Built 3 production Spring Boot microservices handling 10K req/sec",
                  "reason": "Added metrics, scale, and specificity"
                }
              ],
              "rejectionScore": 65,
              "summary": "Overall assessment paragraph"
            }
            
            RESUME:
            %s
            
            JOB DESCRIPTION:
            %s
            
            rejectionScore: likelihood of rejection 0-100 (higher = more likely rejected)
            """.formatted(
                request.getResumeContent(),
                request.getJobDescription()
        );

        String rawResponse = aiProviderService.callWithRetry(
                provider, systemPrompt, userPrompt
        );

        return parseRejectionResponse(rawResponse, request);
    }

    @SuppressWarnings("unchecked")
    private WhyRejectedResponseDTO parseRejectionResponse(
            String rawResponse,
            WhyRejectedRequestDTO request) {
        try {
            String clean = aiProviderService.cleanJson(rawResponse);
            Map<String, Object> parsed = objectMapper.readValue(
                    clean, new TypeReference<>() {}
            );

            List<Map<String, Object>> bulletsList =
                    (List<Map<String, Object>>) parsed.getOrDefault(
                            "bulletImprovements", List.of()
                    );

            List<WhyRejectedResponseDTO.BulletImprovementDTO> bullets =
                    bulletsList.stream()
                            .map(b -> WhyRejectedResponseDTO.BulletImprovementDTO
                                    .builder()
                                    .original((String) b.get("original"))
                                    .improved((String) b.get("improved"))
                                    .reason((String) b.get("reason"))
                                    .build())
                            .toList();

            Integer rejectionScore =
                    (Integer) parsed.getOrDefault("rejectionScore", 50);

            return WhyRejectedResponseDTO.builder()
                    .userId(request.getUserId())
                    .targetRole(request.getTargetRole())
                    .missingSkills(
                            (List<String>) parsed.getOrDefault(
                                    "missingSkills", List.of()
                            )
                    )
                    .weakAreas(
                            (List<String>) parsed.getOrDefault(
                                    "weakAreas", List.of()
                            )
                    )
                    .improvements(
                            (List<String>) parsed.getOrDefault(
                                    "improvements", List.of()
                            )
                    )
                    .bulletImprovements(bullets)
                    .rejectionScore(rejectionScore)
                    .rejectionLabel(getRejectionLabel(rejectionScore))
                    .summary((String) parsed.get("summary"))
                    .build();

        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse rejection response: " + e.getMessage()
            );
        }
    }

    private String getRejectionLabel(Integer score) {
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }
}