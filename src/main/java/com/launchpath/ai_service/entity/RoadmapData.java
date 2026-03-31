package com.launchpath.ai_service.entity;

import com.launchpath.ai_service.entity.base.BaseEntity;
import com.launchpath.ai_service.enums.AiProvider;
import com.launchpath.ai_service.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "roadmap_data",
        indexes = {
                @Index(name = "idx_roadmap_userId", columnList = "userId"),
                @Index(name = "idx_roadmap_userId_role",
                        columnList = "userId, targetRole")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapData extends BaseEntity {

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

    @Column(nullable = false, length = 64)
    private String resumeHash;

    // Duration in weeks (4-8)
    @Column(nullable = false)
    private Integer durationWeeks;

    // Full roadmap stored as JSON
    // [{week: 1, goal: "", skills: [], projects: [], resources: []}]
    @Column(columnDefinition = "TEXT", nullable = false)
    private String roadmapJson;

    @Enumerated(EnumType.STRING)
    private AiProvider provider;

    @Column(nullable = false)
    private Integer version;

    @Column(columnDefinition = "TEXT")
    private String rawAiResponse;
}
