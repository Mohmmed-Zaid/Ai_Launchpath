package com.launchpath.ai_service.dto.response;// ══════════════════════════════════════════════════════════
// dto/response/ReadinessHistoryResponseDTO.java
// ══════════════════════════════════════════════════════════

import com.launchpath.ai_service.enums.ExperienceLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReadinessHistoryResponseDTO {

    private String userId;
    private String targetRole;
    private Integer currentScore;
    private Integer previousScore;
    private Integer scoreDelta;         // improvement since last
    private List<ScorePointDTO> history;

    @Getter
    @Builder
    public static class ScorePointDTO {
        private Integer score;
        private String resumeHash;
        private String trigger;
        private ExperienceLevel experienceLevel;
        private Integer version;
        private LocalDateTime recordedAt;
    }
}