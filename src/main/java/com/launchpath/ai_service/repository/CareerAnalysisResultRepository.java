package com.launchpath.ai_service.repository;

import com.launchpath.ai_service.entity.CareerAnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerAnalysisResultRepository
        extends JpaRepository<CareerAnalysisResult, Long> {

    // Cache check — same user + role + resumeHash = return cached
    Optional<CareerAnalysisResult> findByUserIdAndTargetRoleAndResumeHash(
            String userId, String targetRole, String resumeHash
    );

    // Latest analysis for user + role
    Optional<CareerAnalysisResult> findTopByUserIdAndTargetRoleOrderByVersionDesc(
            String userId, String targetRole
    );

    // Paginated history
    Page<CareerAnalysisResult> findByUserIdOrderByCreatedAtDesc(
            String userId, Pageable pageable
    );

    // Latest version number for incrementing
    @Query("SELECT COALESCE(MAX(c.version), 0) FROM CareerAnalysisResult c " +
            "WHERE c.userId = :userId AND c.targetRole = :targetRole")
    Integer findMaxVersionByUserIdAndTargetRole(
            @Param("userId") String userId,
            @Param("targetRole") String targetRole
    );

    boolean existsByUserIdAndTargetRoleAndResumeHash(
            String userId, String targetRole, String resumeHash
    );
}
