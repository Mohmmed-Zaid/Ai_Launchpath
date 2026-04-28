package com.launchpath.ai_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhyRejectedRequestDTO {


    private String userId;

    @NotBlank(message = "resumeContent is required")
    private String resumeContent;

    @NotBlank(message = "jobDescription is required")
    private String jobDescription;

    private String targetRole;
}
