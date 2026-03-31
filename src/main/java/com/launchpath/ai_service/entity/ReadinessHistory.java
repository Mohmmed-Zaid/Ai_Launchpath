package com.launchpath.ai_service.entity;

import com.launchpath.ai_service.entity.base.BaseEntity;
import com.launchpath.ai_service.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "readiness_history",
        indexes = {
                @Index(name = "idx_rh_userId", columnList = "userId"),
                @Index(name = "idx_rh_userId_role",
                        columnList = "userId, targetRole")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false)
    private Integer readinessScore;

    @Column(nullable = false, length = 64)
    private String resumeHash;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    // What triggered this recalculation
    // "RESUME_UPDATE" | "MANUAL" | "SCHEDULED"
    private String trigger;

    @Column(nullable = false)
    private Integer version;
}