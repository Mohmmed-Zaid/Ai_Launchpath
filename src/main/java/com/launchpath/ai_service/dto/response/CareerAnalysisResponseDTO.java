package com.launchpath.ai_service.dto.response;

import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.enums.ExperienceLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CareerAnalysisResponseDTO {

    private Long id;
    private String userId;
    private String targetRole;
    private ExperienceLevel experienceLevel;
    private String resumeHash;

    // Core output
    private Integer readinessScore;
    private String readinessLabel;     // BEGINNER/DEVELOPING/READY/STRONG
    private List<String> strengths;
    private List<String> skillGaps;
    private List<String> recommendations;

    // Learning links per skill gap
    // key = skill, value = list of resource URLs
    private Map<String, List<String>> learningLinks;

    // Metadata
    private AiProvider provider;
    private Integer version;
    private Boolean fromCache;          // true if returned from DB cache
    private LocalDateTime analyzedAt;
}
