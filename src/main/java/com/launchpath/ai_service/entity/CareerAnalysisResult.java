package com.launchpath.ai_service.entity;

import com.launchpath.ai_service.entity.base.BaseEntity;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "career_analysis_results",
        indexes = {
                @Index(name = "idx_car_userId", columnList = "userId"),
                @Index(name = "idx_car_userId_role_hash",
                        columnList = "userId, targetRole, resumeHash",
                        unique = false)
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CareerAnalysisResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String targetRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;

    // SHA-256 hash of resume content
    // If hash changes → new analysis triggered
    @Column(nullable = false, length = 64)
    private String resumeHash;

    // Readiness score 0-100
    @Column(nullable = false)
    private Integer readinessScore;

    // Stored as JSON strings — PostgreSQL TEXT
    @Column(columnDefinition = "TEXT")
    private String strengthsJson;

    @Column(columnDefinition = "TEXT")
    private String skillGapsJson;

    @Column(columnDefinition = "TEXT")
    private String recommendationsJson;

    // Learning links per skill gap — JSON map
    @Column(columnDefinition = "TEXT")
    private String learningLinksJson;

    // Which AI generated this
    @Enumerated(EnumType.STRING)
    private AiProvider provider;

    // Version — increments on re-analysis
    @Column(nullable = false)
    private Integer version;

    // Raw AI response for debugging
    @Column(columnDefinition = "TEXT")
    private String rawAiResponse;
}
