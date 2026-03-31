// ══════════════════════════════════════════════════════════
// dto/response/WhyRejectedResponseDTO.java
// ══════════════════════════════════════════════════════════
package com.launchpath.ai_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WhyRejectedResponseDTO {

    private String userId;
    private String targetRole;

    private List<String> missingSkills;
    private List<String> weakAreas;
    private List<String> improvements;

    // Specific bullet improvements
    private List<BulletImprovementDTO> bulletImprovements;

    // Overall rejection likelihood 0-100
    private Integer rejectionScore;
    private String rejectionLabel;   // HIGH/MEDIUM/LOW

    private String summary;

    @Getter
    @Builder
    public static class BulletImprovementDTO {
        private String original;
        private String improved;
        private String reason;
    }
}