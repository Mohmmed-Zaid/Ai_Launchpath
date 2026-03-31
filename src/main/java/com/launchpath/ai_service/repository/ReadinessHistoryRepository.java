package com.launchpath.ai_service.repository;

import com.launchpath.ai_service.entity.ReadinessHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadinessHistoryRepository
        extends JpaRepository<ReadinessHistory, Long> {

    // Full history for chart — sorted oldest first
    List<ReadinessHistory> findByUserIdAndTargetRoleOrderByCreatedAtAsc(
            String userId, String targetRole
    );

    // Latest score
    Optional<ReadinessHistory> findTopByUserIdAndTargetRoleOrderByVersionDesc(
            String userId, String targetRole
    );

    // Paginated
    Page<ReadinessHistory> findByUserIdOrderByCreatedAtDesc(
            String userId, Pageable pageable
    );
}