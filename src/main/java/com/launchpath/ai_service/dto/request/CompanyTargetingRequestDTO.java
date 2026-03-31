package com.launchpath.ai_service.dto.request;

import com.launchpath.ai_service.enums.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompanyTargetingRequestDTO {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "targetRole is required")
    private String targetRole;

    private ExperienceLevel experienceLevel;

    private List<String> skills;

    // User location — for local company suggestions
    private String location;

    // Preferred company type
    private String preferredType; // "product", "service", "startup"

    private Integer readinessScore;
}
