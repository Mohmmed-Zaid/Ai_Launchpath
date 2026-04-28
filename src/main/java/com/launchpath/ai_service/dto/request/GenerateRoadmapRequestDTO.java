package com.launchpath.ai_service.dto.request;

import com.launchpath.ai_service.enums.ExperienceLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenerateRoadmapRequestDTO {


    private String userId;

    @NotBlank(message = "targetRole is required")
    private String targetRole;

    @NotNull(message = "experienceLevel is required")
    private ExperienceLevel experienceLevel;

    private List<String> skillGaps;

    private String resumeHash;

    @Min(value = 4, message = "Minimum 4 weeks")
    @Max(value = 8, message = "Maximum 8 weeks")
    private Integer durationWeeks = 6;
}
