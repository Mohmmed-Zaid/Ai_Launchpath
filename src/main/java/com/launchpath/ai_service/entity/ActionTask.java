package com.launchpath.ai_service.entity;

import com.launchpath.ai_service.entity.base.BaseEntity;
import com.launchpath.ai_service.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(
        name = "action_tasks",
        indexes = {
                @Index(name = "idx_task_userId", columnList = "userId"),
                @Index(name = "idx_task_userId_date",
                        columnList = "userId, taskDate")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ActionTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String targetRole;

    // Specific, measurable, time-bound task
    @Column(nullable = false, columnDefinition = "TEXT")
    private String taskDescription;

    // Estimated time to complete
    private String estimatedTime;

    // Skill this task builds
    private String skillCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    // Date this task is assigned for
    @Column(nullable = false)
    private LocalDate taskDate;

    // Priority 1-5
    @Column(nullable = false)
    private Integer priority;
}
