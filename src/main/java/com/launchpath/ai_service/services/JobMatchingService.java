// ══════════════════════════════════════════════════════════
// service/JobMatchingService.java
// ══════════════════════════════════════════════════════════
package com.launchpath.ai_service.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpath.ai_service.dto.request.MatchJobsRequestDTO;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.exception.AiServiceException;
import com.launchpath.ai_service.dto.response.JobMatchResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobMatchingService {

    private final AiProviderService aiProviderService;
    private final ObjectMapper objectMapper;

    public JobMatchResponseDTO matchJobs(
            MatchJobsRequestDTO request,
            AiProvider provider) {

        log.info("Job matching - userId: {}, role: {}",
                request.getUserId(), request.getTargetRole());

        String systemPrompt =
                "You are a job placement expert. Match candidates to relevant jobs. " +
                        "Respond with valid JSON only.";

        String userPrompt = """
            Generate ranked job matches and return JSON:
            {
              "jobs": [
                {
                  "title": "Job Title",
                  "company": "Company Name",
                  "location": "City, Country",
                  "salaryRange": "10-15 LPA",
                  "matchScore": 85,
                  "matchedSkills": ["Java", "Spring"],
                  "missingSkills": ["Kubernetes"],
                  "applyUrl": "https://...",
                  "experienceRequired": "2-4 years",
                  "whyGoodFit": "reason this is a good match"
                }
              ]
            }
            
            TARGET ROLE: %s
            EXPERIENCE: %s
            SKILLS: %s
            LOCATION: %s
            READINESS SCORE: %d
            
            Return %d jobs, sorted by matchScore descending.
            matchScore = how well candidate fits this specific job (0-100).
            """.formatted(
                request.getTargetRole(),
                request.getExperienceLevel(),
                request.getSkills() != null
                        ? request.getSkills() : List.of(),
                request.getLocation() != null
                        ? request.getLocation() : "India",
                request.getReadinessScore() != null
                        ? request.getReadinessScore() : 50,
                request.getSize()
        );

        String rawResponse = aiProviderService.callWithRetry(
                provider, systemPrompt, userPrompt
        );

        return parseJobResponse(rawResponse, request);
    }

    @SuppressWarnings("unchecked")
    private JobMatchResponseDTO parseJobResponse(
            String rawResponse,
            MatchJobsRequestDTO request) {
        try {
            String clean = aiProviderService.cleanJson(rawResponse);
            Map<String, Object> parsed = objectMapper.readValue(
                    clean, new TypeReference<>() {}
            );

            List<Map<String, Object>> jobsList =
                    (List<Map<String, Object>>) parsed.getOrDefault(
                            "jobs", List.of()
                    );

            List<JobMatchResponseDTO.JobDTO> jobs = jobsList.stream()
                    .map(j -> JobMatchResponseDTO.JobDTO.builder()
                            .title((String) j.get("title"))
                            .company((String) j.get("company"))
                            .location((String) j.get("location"))
                            .salaryRange((String) j.get("salaryRange"))
                            .matchScore((Integer) j.get("matchScore"))
                            .matchedSkills(
                                    (List<String>) j.getOrDefault(
                                            "matchedSkills", List.of()
                                    )
                            )
                            .missingSkills(
                                    (List<String>) j.getOrDefault(
                                            "missingSkills", List.of()
                                    )
                            )
                            .applyUrl((String) j.get("applyUrl"))
                            .experienceRequired(
                                    (String) j.get("experienceRequired")
                            )
                            .whyGoodFit((String) j.get("whyGoodFit"))
                            .build())
                    .toList();

            return JobMatchResponseDTO.builder()
                    .userId(request.getUserId())
                    .targetRole(request.getTargetRole())
                    .totalMatches(jobs.size())
                    .page(request.getPage())
                    .size(request.getSize())
                    .jobs(jobs)
                    .build();

        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse job matches: " + e.getMessage()
            );
        }
    }
}