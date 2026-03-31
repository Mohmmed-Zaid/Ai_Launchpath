// ══════════════════════════════════════════════════════════
// service/ReadinessHistoryService.java
// ══════════════════════════════════════════════════════════
package com.launchpath.ai_service.services;


import com.launchpath.ai_service.dto.response.ReadinessHistoryResponseDTO;
import com.launchpath.ai_service.entity.ReadinessHistory;
import com.launchpath.ai_service.repository.ReadinessHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadinessHistoryService {

    private final ReadinessHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public ReadinessHistoryResponseDTO getHistory(String userId,
                                                  String targetRole) {
        List<ReadinessHistory> history = historyRepository
                .findByUserIdAndTargetRoleOrderByCreatedAtAsc(
                        userId, targetRole
                );

        // Score delta — improvement since last analysis
        Integer currentScore = null;
        Integer previousScore = null;

        if (!history.isEmpty()) {
            currentScore = history.get(history.size() - 1)
                    .getReadinessScore();
            if (history.size() > 1) {
                previousScore = history.get(history.size() - 2)
                        .getReadinessScore();
            }
        }

        Integer delta = (currentScore != null && previousScore != null)
                ? currentScore - previousScore
                : null;

        List<ReadinessHistoryResponseDTO.ScorePointDTO> points =
                history.stream()
                        .map(h -> ReadinessHistoryResponseDTO.ScorePointDTO
                                .builder()
                                .score(h.getReadinessScore())
                                .resumeHash(h.getResumeHash())
                                .trigger(h.getTrigger())
                                .experienceLevel(h.getExperienceLevel())
                                .version(h.getVersion())
                                .recordedAt(h.getCreatedAt())
                                .build())
                        .toList();

        return ReadinessHistoryResponseDTO.builder()
                .userId(userId)
                .targetRole(targetRole)
                .currentScore(currentScore)
                .previousScore(previousScore)
                .scoreDelta(delta)
                .history(points)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ReadinessHistoryResponseDTO> getPagedHistory(
            String userId, Pageable pageable) {
        return historyRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(h -> ReadinessHistoryResponseDTO.builder()
                        .userId(h.getUserId())
                        .targetRole(h.getTargetRole())
                        .currentScore(h.getReadinessScore())
                        .history(List.of(
                                ReadinessHistoryResponseDTO.ScorePointDTO
                                        .builder()
                                        .score(h.getReadinessScore())
                                        .trigger(h.getTrigger())
                                        .version(h.getVersion())
                                        .recordedAt(h.getCreatedAt())
                                        .build()
                        ))
                        .build());
    }
}