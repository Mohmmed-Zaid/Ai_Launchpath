package com.launchpath.ai_service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpath.ai_service.dto.request.AnalyzeCareerRequestDTO;
import com.launchpath.ai_service.dto.response.CareerAnalysisResponseDTO;
import com.launchpath.ai_service.entity.CareerAnalysisResult;
import com.launchpath.ai_service.entity.ReadinessHistory;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.exception.AiServiceException;
import com.launchpath.ai_service.repository.CareerAnalysisResultRepository;
import com.launchpath.ai_service.repository.ReadinessHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerAnalysisService {

    private final CareerAnalysisResultRepository analysisRepository;
    private final ReadinessHistoryRepository readinessHistoryRepository;
    private final AiProviderService aiProviderService;
    private final ObjectMapper objectMapper;

    @Transactional
    public CareerAnalysisResponseDTO analyzeCareer(
            AnalyzeCareerRequestDTO request,
            AiProvider provider) {

        log.info("Career analysis - userId: {}, role: {}",
                request.getUserId(), request.getTargetRole());

        // 1. Cache check
        if (request.getResumeHash() != null) {
            Optional<CareerAnalysisResult> cached =
                    analysisRepository.findByUserIdAndTargetRoleAndResumeHash(
                            request.getUserId(),
                            request.getTargetRole(),
                            request.getResumeHash()
                    );
            if (cached.isPresent()) {
                log.info("Cache hit - returning cached analysis");
                return mapToDTO(cached.get(), true);
            }
        }

        // 2. Build prompts
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildCareerAnalysisPrompt(request);

        // 3. Call AI — provider passed correctly
        String rawResponse = aiProviderService.callWithRetry(
                provider, systemPrompt, userPrompt
        );

        // 4. Parse response
        Map<String, Object> parsed = parseAiResponse(rawResponse);

        // 5. Get next version
        Integer nextVersion = analysisRepository
                .findMaxVersionByUserIdAndTargetRole(
                        request.getUserId(), request.getTargetRole()
                ) + 1;

        // 6. Save to DB
        CareerAnalysisResult result = CareerAnalysisResult.builder()
                .userId(request.getUserId())
                .targetRole(request.getTargetRole())
                .experienceLevel(request.getExperienceLevel())
                .resumeHash(request.getResumeHash() != null
                        ? request.getResumeHash() : "no-hash")
                .readinessScore(
                        (Integer) parsed.getOrDefault("readinessScore", 0)
                )
                .strengthsJson(toJson(parsed.get("strengths")))
                .skillGapsJson(toJson(parsed.get("skillGaps")))
                .recommendationsJson(toJson(parsed.get("recommendations")))
                .learningLinksJson(toJson(parsed.get("learningLinks")))
                .provider(provider)
                .version(nextVersion)
                .rawAiResponse(rawResponse)
                .build();

        CareerAnalysisResult saved = analysisRepository.save(result);

        // 7. Save readiness history
        saveReadinessHistory(saved, "MANUAL");

        log.info("Career analysis complete - score: {}, version: {}",
                saved.getReadinessScore(), saved.getVersion());

        return mapToDTO(saved, false);
    }

    @Transactional(readOnly = true)
    public Page<CareerAnalysisResponseDTO> getAnalysisHistory(
            String userId, Pageable pageable) {
        return analysisRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(r -> mapToDTO(r, false));
    }

    private void saveReadinessHistory(CareerAnalysisResult result,
                                      String trigger) {
        ReadinessHistory history = ReadinessHistory.builder()
                .userId(result.getUserId())
                .targetRole(result.getTargetRole())
                .readinessScore(result.getReadinessScore())
                .resumeHash(result.getResumeHash())
                .experienceLevel(result.getExperienceLevel())
                .trigger(trigger)
                .version(result.getVersion())
                .build();
        readinessHistoryRepository.save(history);
    }

    @SuppressWarnings("unchecked")
    private CareerAnalysisResponseDTO mapToDTO(CareerAnalysisResult r,
                                               boolean fromCache) {
        return CareerAnalysisResponseDTO.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .targetRole(r.getTargetRole())
                .experienceLevel(r.getExperienceLevel())
                .resumeHash(r.getResumeHash())
                .readinessScore(r.getReadinessScore())
                .readinessLabel(getReadinessLabel(r.getReadinessScore()))
                .strengths(fromJson(r.getStrengthsJson(), List.class))
                .skillGaps(fromJson(r.getSkillGapsJson(), List.class))
                .recommendations(fromJson(r.getRecommendationsJson(), List.class))
                .learningLinks(fromJson(r.getLearningLinksJson(), Map.class))
                .provider(r.getProvider())
                .version(r.getVersion())
                .fromCache(fromCache)
                .analyzedAt(r.getCreatedAt())
                .build();
    }

    private String getReadinessLabel(Integer score) {
        if (score == null) return "UNKNOWN";
        if (score >= 80)   return "STRONG";
        if (score >= 60)   return "READY";
        if (score >= 40)   return "DEVELOPING";
        return "BEGINNER";
    }

    private String buildSystemPrompt() {
        return "You are a senior career coach and technical recruiter. " +
                "Analyze resumes and provide structured career guidance. " +
                "Always respond with valid JSON only. No markdown. No explanation.";
    }

    private String buildCareerAnalysisPrompt(AnalyzeCareerRequestDTO req) {
        return """
            Analyze this candidate's career profile and return JSON:
            {
              "readinessScore": <0-100>,
              "strengths": ["strength1", "strength2"],
              "skillGaps": ["gap1", "gap2"],
              "recommendations": ["rec1", "rec2"],
              "learningLinks": {
                "skill1": ["https://resource1", "https://resource2"],
                "skill2": ["https://resource1"]
              }
            }
            
            TARGET ROLE: %s
            EXPERIENCE LEVEL: %s
            CURRENT SKILLS: %s
            PROJECTS: %s
            EXPERIENCE: %s
            EDUCATION: %s
            
            readinessScore: How ready is this person for the target role (0-100)?
            skillGaps: What specific skills are missing for this role?
            learningLinks: For each skill gap, provide 2-3 real learning resources.
            """.formatted(
                req.getTargetRole(),
                req.getExperienceLevel(),
                req.getSkills() != null ? req.getSkills() : List.of(),
                req.getResumeData() != null
                        ? req.getResumeData().getProjects() : List.of(),
                req.getResumeData() != null
                        ? req.getResumeData().getExperience() : List.of(),
                req.getResumeData() != null
                        ? req.getResumeData().getEducation() : List.of()
        );
    }

    private Map<String, Object> parseAiResponse(String rawResponse) {
        try {
            String clean = aiProviderService.cleanJson(rawResponse);
            return objectMapper.readValue(
                    clean, new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse AI response: " + e.getMessage()
            );
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}