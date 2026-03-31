package com.launchpath.ai_service.dto.response;

import com.launchpath.ai_service.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ActionSuggestionResponseDTO {

    private String userId;
    private String targetRole;
    private LocalDate taskDate;
    private List<TaskDTO> tasks;

    @Getter
    @Builder
    public static class TaskDTO {
        private Long id;
        private String taskDescription;
        private String estimatedTime;
        private String skillCategory;
        private Integer priority;
        private TaskStatus status;
    }
}