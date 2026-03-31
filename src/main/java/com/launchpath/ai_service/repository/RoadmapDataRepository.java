package com.launchpath.ai_service.repository;

import com.launchpath.ai_service.entity.RoadmapData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoadmapDataRepository
        extends JpaRepository<RoadmapData, Long> {

    // Cache check
    Optional<RoadmapData> findByUserIdAndTargetRoleAndResumeHash(
            String userId, String targetRole, String resumeHash
    );

    // Latest roadmap
    Optional<RoadmapData> findTopByUserIdAndTargetRoleOrderByVersionDesc(
            String userId, String targetRole
    );

    // Paginated history
    Page<RoadmapData> findByUserIdOrderByCreatedAtDesc(
            String userId, Pageable pageable
    );

    @Query("SELECT COALESCE(MAX(r.version), 0) FROM RoadmapData r " +
            "WHERE r.userId = :userId AND r.targetRole = :targetRole")
    Integer findMaxVersionByUserIdAndTargetRole(
            @Param("userId") String userId,
            @Param("targetRole") String targetRole
    );
}

