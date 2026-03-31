package com.launchpath.ai_service.dto.response;


import com.launchpath.ai_service.enums.AiProvider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RoadmapResponseDTO {

    private Long id;
    private String userId;
    private String targetRole;
    private Integer durationWeeks;
    private Integer version;
    private Boolean fromCache;
    private AiProvider provider;
    private LocalDateTime generatedAt;

    // Weekly breakdown
    private List<WeekDTO> weeks;

    @Getter
    @Builder
    public static class WeekDTO {
        private Integer weekNumber;
        private String goal;
        private List<String> skills;
        private List<String> projects;
        private List<String> resources;
        private String milestoneCheck;
    }
}
