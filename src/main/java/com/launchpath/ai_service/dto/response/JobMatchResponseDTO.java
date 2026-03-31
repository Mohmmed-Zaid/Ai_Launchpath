// ══════════════════════════════════════════════════════════
// dto/response/JobMatchResponseDTO.java
// ══════════════════════════════════════════════════════════
package com.launchpath.ai_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JobMatchResponseDTO {

    private String userId;
    private String targetRole;
    private Integer totalMatches;
    private Integer page;
    private Integer size;
    private List<JobDTO> jobs;

    @Getter
    @Builder
    public static class JobDTO {
        private String title;
        private String company;
        private String location;
        private String salaryRange;
        private Integer matchScore;       // 0-100 fit score
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private String applyUrl;
        private String experienceRequired;
        private String whyGoodFit;
    }
}