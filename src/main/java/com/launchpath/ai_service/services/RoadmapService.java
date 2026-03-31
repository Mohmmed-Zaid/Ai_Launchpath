// ══════════════════════════════════════════════════════════
// service/RoadmapService.java
// ══════════════════════════════════════════════════════════
package com.launchpath.ai_service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpath.ai_service.dto.request.GenerateRoadmapRequestDTO;
import com.launchpath.ai_service.dto.response.RoadmapResponseDTO;
import com.launchpath.ai_service.entity.RoadmapData;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.repository.RoadmapDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapDataRepository roadmapRepository;
    private final AiProviderService aiProviderService;
    private final ObjectMapper objectMapper;

    @Transactional
    public RoadmapResponseDTO generateRoadmap(
            GenerateRoadmapRequestDTO request,
            AiProvider provider) {

        log.info("Generating roadmap - userId: {}, role: {}",
                request.getUserId(), request.getTargetRole());

        // Cache check
        if (request.getResumeHash() != null) {
            Optional<RoadmapData> cached =
                    roadmapRepository.findByUserIdAndTargetRoleAndResumeHash(
                            request.getUserId(),
                            request.getTargetRole(),
                            request.getResumeHash()
                    );
            if (cached.isPresent()) {
                log.info("Returning cached roadmap");
                return mapToDTO(cached.get(), true);
            }
        }

        String systemPrompt = "You are a senior engineering mentor. " +
                "Create structured learning roadmaps. " +
                "Respond with valid JSON only.";

        String userPrompt = """
            Generate a %d-week roadmap for this person:
            {
              "weeks": [
                {
                  "weekNumber": 1,
                  "goal": "specific weekly goal",
                  "skills": ["skill1", "skill2"],
                  "projects": ["project to build"],
                  "resources": ["https://resource1"],
                  "milestoneCheck": "how to verify completion"
                }
              ]
            }
            
            TARGET ROLE: %s
            EXPERIENCE: %s
            SKILL GAPS: %s
            
            Make each week specific, actionable, and progressive.
            """.formatted(
                request.getDurationWeeks(),
                request.getTargetRole(),
                request.getExperienceLevel(),
                request.getSkillGaps() != null
                        ? request.getSkillGaps() : List.of()
        );

        String rawResponse = aiProviderService.callWithRetry(
                provider, systemPrompt, userPrompt
        );

        Integer nextVersion = roadmapRepository
                .findMaxVersionByUserIdAndTargetRole(
                        request.getUserId(), request.getTargetRole()
                ) + 1;

        RoadmapData roadmap = RoadmapData.builder()
                .userId(request.getUserId())
                .targetRole(request.getTargetRole())
                .experienceLevel(request.getExperienceLevel())
                .resumeHash(request.getResumeHash() != null
                        ? request.getResumeHash() : "no-hash")
                .durationWeeks(request.getDurationWeeks())
                .roadmapJson(aiProviderService.cleanJson(rawResponse))
                .provider(provider)
                .version(nextVersion)
                .rawAiResponse(rawResponse)
                .build();

        RoadmapData saved = roadmapRepository.save(roadmap);
        log.info("Roadmap saved - id: {}", saved.getId());
        return mapToDTO(saved, false);
    }

    @Transactional(readOnly = true)
    public Page<RoadmapResponseDTO> getRoadmapHistory(
            String userId, Pageable pageable) {
        return roadmapRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(r -> mapToDTO(r, false));
    }

    @SuppressWarnings("unchecked")
    private RoadmapResponseDTO mapToDTO(RoadmapData r, boolean fromCache) {
        List<RoadmapResponseDTO.WeekDTO> weeks = new ArrayList<>();

        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    r.getRoadmapJson(),
                    new TypeReference<>() {}
            );
            List<Map<String, Object>> weeksList =
                    (List<Map<String, Object>>) parsed.get("weeks");

            if (weeksList != null) {
                weeksList.forEach(w -> weeks.add(
                        RoadmapResponseDTO.WeekDTO.builder()
                                .weekNumber((Integer) w.get("weekNumber"))
                                .goal((String) w.get("goal"))
                                .skills((List<String>) w.get("skills"))
                                .projects((List<String>) w.get("projects"))
                                .resources((List<String>) w.get("resources"))
                                .milestoneCheck((String) w.get("milestoneCheck"))
                                .build()
                ));
            }
        } catch (Exception e) {
            log.error("Failed to parse roadmap JSON: {}", e.getMessage());
        }

        return RoadmapResponseDTO.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .targetRole(r.getTargetRole())
                .durationWeeks(r.getDurationWeeks())
                .version(r.getVersion())
                .fromCache(fromCache)
                .provider(r.getProvider())
                .generatedAt(r.getCreatedAt())
                .weeks(weeks)
                .build();
    }
}