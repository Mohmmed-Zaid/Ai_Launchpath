package com.launchpath.ai_service.dto.request;

import com.launchpath.ai_service.enums.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SuggestActionsRequestDTO {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "targetRole is required")
    private String targetRole;

    private ExperienceLevel experienceLevel;

    // Current skill gaps — from career analysis
    private List<String> skillGaps;

    // How many tasks to generate
    private Integer taskCount = 5;
}
