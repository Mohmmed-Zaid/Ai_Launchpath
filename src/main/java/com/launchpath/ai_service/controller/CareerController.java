package com.launchpath.ai_service.controller;

import com.launchpath.ai_service.dto.request.*;
import com.launchpath.ai_service.dto.response.*;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.exception.RateLimitExceededException;
import com.launchpath.ai_service.feign.UserServiceClient;
import com.launchpath.ai_service.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class CareerController {

    private final CareerAnalysisService careerAnalysisService;
    private final RoadmapService roadmapService;
    private final ActionSuggestionService actionSuggestionService;
    private final CompanyTargetingService companyTargetingService;
    private final JobMatchingService jobMatchingService;
    private final WhyRejectedService whyRejectedService;
    private final ReadinessHistoryService readinessHistoryService;
    private final RateLimitService rateLimitService;
    private final UserServiceClient userServiceClient;

    // ── analyze-career ────────────────────────────────────

    @PostMapping("/analyze-career")
    public ResponseEntity<ApiResponseDTO<CareerAnalysisResponseDTO>>
    analyzeCareer(
            @Valid @RequestBody AnalyzeCareerRequestDTO request) {

        Long userId = getCurrentUserId();
        // Sync request userId with JWT userId
        request.setUserId(String.valueOf(userId));

        checkRateLimit(String.valueOf(userId));
        AiProvider provider = resolveProvider(String.valueOf(userId));

        CareerAnalysisResponseDTO result =
                careerAnalysisService.analyzeCareer(request, provider);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Career analysis complete", result)
        );
    }

    // ── generate-roadmap ──────────────────────────────────

    @PostMapping("/generate-roadmap")
    public ResponseEntity<ApiResponseDTO<RoadmapResponseDTO>>
    generateRoadmap(
            @Valid @RequestBody GenerateRoadmapRequestDTO request) {

        Long userId = getCurrentUserId();
        request.setUserId(String.valueOf(userId));

        checkRateLimit(String.valueOf(userId));
        AiProvider provider = resolveProvider(String.valueOf(userId));

        RoadmapResponseDTO result =
                roadmapService.generateRoadmap(request, provider);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Roadmap generated", result)
        );
    }

    // ── suggest-actions ───────────────────────────────────

    @PostMapping("/suggest-actions")
    public ResponseEntity<ApiResponseDTO<ActionSuggestionResponseDTO>>
    suggestActions(
            @Valid @RequestBody SuggestActionsRequestDTO request) {

        Long userId = getCurrentUserId();
        request.setUserId(String.valueOf(userId));

        checkRateLimit(String.valueOf(userId));
        AiProvider provider = resolveProvider(String.valueOf(userId));

        ActionSuggestionResponseDTO result =
                actionSuggestionService.suggestActions(request, provider);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Daily tasks generated", result)
        );
    }

    // ── company-targeting ─────────────────────────────────

    @PostMapping("/company-targeting")
    public ResponseEntity<ApiResponseDTO<CompanyTargetingResponseDTO>>
    companyTargeting(
            @Valid @RequestBody CompanyTargetingRequestDTO request) {

        Long userId = getCurrentUserId();
        request.setUserId(String.valueOf(userId));

        checkRateLimit(String.valueOf(userId));
        AiProvider provider = resolveProvider(String.valueOf(userId));

        CompanyTargetingResponseDTO result =
                companyTargetingService.targetCompanies(request, provider);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Company targets generated", result)
        );
    }

    // ── match-jobs ────────────────────────────────────────

    @PostMapping("/match-jobs")
    public ResponseEntity<ApiResponseDTO<JobMatchResponseDTO>>
    matchJobs(
            @Valid @RequestBody MatchJobsRequestDTO request) {

        Long userId = getCurrentUserId();
        request.setUserId(String.valueOf(userId));

        checkRateLimit(String.valueOf(userId));
        AiProvider provider = resolveProvider(String.valueOf(userId));

        JobMatchResponseDTO result =
                jobMatchingService.matchJobs(request, provider);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Job matches generated", result)
        );
    }

    // ── why-rejected ──────────────────────────────────────

    @PostMapping("/why-rejected")
    public ResponseEntity<ApiResponseDTO<WhyRejectedResponseDTO>>
    whyRejected(
            @Valid @RequestBody WhyRejectedRequestDTO request) {

        Long userId = getCurrentUserId();
        request.setUserId(String.valueOf(userId));

        checkRateLimit(String.valueOf(userId));
        AiProvider provider = resolveProvider(String.valueOf(userId));

        WhyRejectedResponseDTO result =
                whyRejectedService.analyzeRejection(request, provider);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Rejection analysis complete", result)
        );
    }

    // ── analysis-history ──────────────────────────────────

    @GetMapping("/analysis-history")
    public ResponseEntity<ApiResponseDTO<Page<CareerAnalysisResponseDTO>>>
    getAnalysisHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {

        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(sort).descending()
        );

        Page<CareerAnalysisResponseDTO> history =
                careerAnalysisService.getAnalysisHistory(
                        String.valueOf(userId), pageable
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Analysis history fetched", history)
        );
    }

    // ── roadmap-history ───────────────────────────────────

    @GetMapping("/roadmap-history")
    public ResponseEntity<ApiResponseDTO<Page<RoadmapResponseDTO>>>
    getRoadmapHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<RoadmapResponseDTO> history =
                roadmapService.getRoadmapHistory(
                        String.valueOf(userId), pageable
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Roadmap history fetched", history)
        );
    }

    // ── readiness-history ─────────────────────────────────

    @GetMapping("/readiness-history")
    public ResponseEntity<ApiResponseDTO<ReadinessHistoryResponseDTO>>
    getReadinessHistory(
            @RequestParam String targetRole) {

        Long userId = getCurrentUserId();

        ReadinessHistoryResponseDTO history =
                readinessHistoryService.getHistory(
                        String.valueOf(userId), targetRole
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Readiness history fetched", history)
        );
    }

    // ── tasks/today ───────────────────────────────────────

    @GetMapping("/tasks/today")
    public ResponseEntity<ApiResponseDTO<ActionSuggestionResponseDTO>>
    getTodaysTasks(
            @RequestParam String targetRole) {

        Long userId = getCurrentUserId();

        ActionSuggestionResponseDTO tasks =
                actionSuggestionService.getTodaysTasks(
                        String.valueOf(userId), targetRole
                );

        return ResponseEntity.ok(
                ApiResponseDTO.success("Today's tasks fetched", tasks)
        );
    }

    // ── Private helpers ───────────────────────────────────

    private void checkRateLimit(String userId) {
        if (!rateLimitService.isAllowed(userId)) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Please wait before making more requests."
            );
        }
    }

    private AiProvider resolveProvider(String userId) {
        if (userId == null) return AiProvider.GROQ_FREE;

        try {
            Integer remainingCredits = userServiceClient
                    .getRemainingAtsCredits(userId)
                    .getData();

            if (remainingCredits != null && remainingCredits > 3) {
                log.info("Paid user — using Groq Pro for userId: {}", userId);
                return AiProvider.GROQ_PRO;
            }
        } catch (Exception e) {
            log.warn("Could not check plan — defaulting to Groq Free");
        }

        return AiProvider.GROQ_FREE;
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}