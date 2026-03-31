package com.launchpath.ai_service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpath.ai_service.dto.request.CompanyTargetingRequestDTO;
import com.launchpath.ai_service.dto.response.CompanyTargetingResponseDTO;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.enums.CompanyTier;
import com.launchpath.ai_service.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyTargetingService {

    private final AiProviderService aiProviderService;
    private final ObjectMapper objectMapper;

    public CompanyTargetingResponseDTO targetCompanies(
            CompanyTargetingRequestDTO request,
            AiProvider provider) {

        log.info("Company targeting - userId: {}, role: {}",
                request.getUserId(), request.getTargetRole());

        String systemPrompt =
                "You are a career placement expert. " +
                        "Suggest real companies for candidates. " +
                        "Respond with valid JSON only.";

        String userPrompt = """
            Suggest companies for this candidate and return JSON:
            {
              "tier1": [
                {
                  "name": "Company Name",
                  "type": "service/product/startup",
                  "applyUrl": "https://careers.company.com",
                  "whyGoodFit": "reason",
                  "salaryRange": "8-12 LPA",
                  "requiredSkills": ["Java", "Spring Boot"]
                }
              ],
              "tier2": [],
              "tier3": [],
              "advice": "overall application strategy advice"
            }
            
            TARGET ROLE: %s
            EXPERIENCE: %s
            SKILLS: %s
            LOCATION: %s
            READINESS SCORE: %d
            PREFERRED TYPE: %s
            
            tier1 = easy to get (service companies, mass hiring)
            tier2 = mid-level (good product companies)
            tier3 = high-paying (FAANG, top product, high-growth startups)
            """.formatted(
                request.getTargetRole(),
                request.getExperienceLevel(),
                request.getSkills() != null
                        ? request.getSkills() : List.of(),
                request.getLocation() != null
                        ? request.getLocation() : "India",
                request.getReadinessScore() != null
                        ? request.getReadinessScore() : 50,
                request.getPreferredType() != null
                        ? request.getPreferredType() : "any"
        );

        String rawResponse = aiProviderService.callWithRetry(
                provider, systemPrompt, userPrompt
        );

        return parseCompanyResponse(rawResponse, request);
    }

    @SuppressWarnings("unchecked")
    private CompanyTargetingResponseDTO parseCompanyResponse(
            String rawResponse,
            CompanyTargetingRequestDTO request) {
        try {
            String clean = aiProviderService.cleanJson(rawResponse);
            Map<String, Object> parsed = objectMapper.readValue(
                    clean, new TypeReference<>() {}
            );

            return CompanyTargetingResponseDTO.builder()
                    .userId(request.getUserId())
                    .targetRole(request.getTargetRole())
                    .location(request.getLocation())
                    .tier1Companies(
                            parseCompanyList(parsed.get("tier1"), CompanyTier.TIER_1)
                    )
                    .tier2Companies(
                            parseCompanyList(parsed.get("tier2"), CompanyTier.TIER_2)
                    )
                    .tier3Companies(
                            parseCompanyList(parsed.get("tier3"), CompanyTier.TIER_3)
                    )
                    .advice((String) parsed.get("advice"))
                    .build();

        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse company response: " + e.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private List<CompanyTargetingResponseDTO.CompanyDTO> parseCompanyList(
            Object raw, CompanyTier tier) {

        if (!(raw instanceof List)) return new ArrayList<>();
        List<Map<String, Object>> list = (List<Map<String, Object>>) raw;

        return list.stream()
                .map(c -> CompanyTargetingResponseDTO.CompanyDTO.builder()
                        .name((String) c.get("name"))
                        .tier(tier)
                        .type((String) c.get("type"))
                        .applyUrl((String) c.get("applyUrl"))
                        .whyGoodFit((String) c.get("whyGoodFit"))
                        .salaryRange((String) c.get("salaryRange"))
                        .requiredSkills(
                                (List<String>) c.getOrDefault(
                                        "requiredSkills", List.of()
                                )
                        )
                        .build())
                .toList();
    }
}