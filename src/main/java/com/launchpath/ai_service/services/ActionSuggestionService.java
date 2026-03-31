package com.launchpath.ai_service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpath.ai_service.dto.request.SuggestActionsRequestDTO;
import com.launchpath.ai_service.dto.response.ActionSuggestionResponseDTO;
import com.launchpath.ai_service.entity.ActionTask;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.enums.TaskStatus;
import com.launchpath.ai_service.exception.AiServiceException;
import com.launchpath.ai_service.repository.ActionTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionSuggestionService {

    private final ActionTaskRepository taskRepository;
    private final AiProviderService aiProviderService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ActionSuggestionResponseDTO suggestActions(
            SuggestActionsRequestDTO request,
            AiProvider provider) {

        log.info("Suggesting actions - userId: {}", request.getUserId());

        String systemPrompt =
                "You are a career coach. Generate specific daily tasks. " +
                        "Respond with valid JSON only.";

        String userPrompt = """
            Generate %d specific daily tasks for this developer:
            {
              "tasks": [
                {
                  "taskDescription": "specific measurable task",
                  "estimatedTime": "2 hours",
                  "skillCategory": "Spring Boot",
                  "priority": 1
                }
              ]
            }
            
            TARGET ROLE: %s
            EXPERIENCE: %s
            SKILL GAPS: %s
            
            Tasks must be:
            - Specific (not vague)
            - Measurable (clear completion criteria)
            - Time-bound (with estimate)
            - Progressive (building toward the role)
            """.formatted(
                request.getTaskCount(),
                request.getTargetRole(),
                request.getExperienceLevel(),
                request.getSkillGaps() != null
                        ? request.getSkillGaps() : List.of()
        );

        String rawResponse = aiProviderService.callWithRetry(
                provider, systemPrompt, userPrompt
        );

        List<ActionTask> savedTasks = parsAndSaveTasks(
                rawResponse, request
        );

        return mapToDTO(savedTasks, request);
    }

    @Transactional
    public ActionTask updateTaskStatus(Long taskId,
                                       String userId,
                                       TaskStatus newStatus) {
        ActionTask task = taskRepository.findById(taskId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> new AiServiceException(
                        "Task not found: " + taskId
                ));
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public ActionSuggestionResponseDTO getTodaysTasks(
            String userId, String targetRole) {

        List<ActionTask> tasks = taskRepository
                .findByUserIdAndTaskDateOrderByPriorityAsc(
                        userId, LocalDate.now()
                );
        return mapToDTO(tasks,
                buildStubRequest(userId, targetRole));
    }

    @SuppressWarnings("unchecked")
    private List<ActionTask> parsAndSaveTasks(
            String rawResponse,
            SuggestActionsRequestDTO request) {
        try {
            String clean = aiProviderService.cleanJson(rawResponse);
            Map<String, Object> parsed = objectMapper.readValue(
                    clean, new TypeReference<>() {}
            );
            List<Map<String, Object>> tasksList =
                    (List<Map<String, Object>>) parsed.get("tasks");

            List<ActionTask> tasks = new ArrayList<>();
            int priority = 1;

            for (Map<String, Object> t : tasksList) {
                ActionTask task = ActionTask.builder()
                        .userId(request.getUserId())
                        .targetRole(request.getTargetRole())
                        .taskDescription((String) t.get("taskDescription"))
                        .estimatedTime((String) t.get("estimatedTime"))
                        .skillCategory((String) t.get("skillCategory"))
                        .status(TaskStatus.PENDING)
                        .taskDate(LocalDate.now())
                        .priority(priority++)
                        .build();
                tasks.add(task);
            }

            return taskRepository.saveAll(tasks);

        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse tasks: " + e.getMessage()
            );
        }
    }

    private ActionSuggestionResponseDTO mapToDTO(
            List<ActionTask> tasks,
            SuggestActionsRequestDTO request) {

        List<ActionSuggestionResponseDTO.TaskDTO> taskDTOs = tasks.stream()
                .map(t -> ActionSuggestionResponseDTO.TaskDTO.builder()
                        .id(t.getId())
                        .taskDescription(t.getTaskDescription())
                        .estimatedTime(t.getEstimatedTime())
                        .skillCategory(t.getSkillCategory())
                        .priority(t.getPriority())
                        .status(t.getStatus())
                        .build())
                .toList();

        return ActionSuggestionResponseDTO.builder()
                .userId(request.getUserId())
                .targetRole(request.getTargetRole())
                .taskDate(LocalDate.now())
                .tasks(taskDTOs)
                .build();
    }

    private SuggestActionsRequestDTO buildStubRequest(String userId,
                                                      String targetRole) {
        SuggestActionsRequestDTO req = new SuggestActionsRequestDTO();
        req.setUserId(userId);
        req.setTargetRole(targetRole);
        return req;
    }
}