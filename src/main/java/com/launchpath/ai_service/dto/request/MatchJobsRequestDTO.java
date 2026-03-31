package com.launchpath.ai_service.dto.request;

import com.launchpath.ai_service.enums.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatchJobsRequestDTO {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "targetRole is required")
    private String targetRole;

    private ExperienceLevel experienceLevel;

    private List<String> skills;

    private Integer readinessScore;

    private String location;

    // Pagination
    private Integer page = 0;
    private Integer size = 10;
}
