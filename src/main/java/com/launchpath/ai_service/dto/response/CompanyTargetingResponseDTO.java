// ══════════════════════════════════════════════════════════
// dto/response/CompanyTargetingResponseDTO.java
// ══════════════════════════════════════════════════════════
package com.launchpath.ai_service.dto.response;

import com.launchpath.ai_service.enums.CompanyTier;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CompanyTargetingResponseDTO {

    private String userId;
    private String targetRole;
    private String location;

    private List<CompanyDTO> tier1Companies;
    private List<CompanyDTO> tier2Companies;
    private List<CompanyDTO> tier3Companies;

    private String advice;

    @Getter
    @Builder
    public static class CompanyDTO {
        private String name;
        private CompanyTier tier;
        private String type;          // product/service/startup
        private String applyUrl;
        private String whyGoodFit;
        private String salaryRange;
        private List<String> requiredSkills;
    }
}