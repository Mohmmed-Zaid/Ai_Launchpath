package com.launchpath.ai_service.repository;

import com.launchpath.ai_service.entity.ActionTask;
import com.launchpath.ai_service.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActionTaskRepository
        extends JpaRepository<ActionTask, Long> {

    // Today's tasks for user
    List<ActionTask> findByUserIdAndTaskDateOrderByPriorityAsc(
            String userId, LocalDate taskDate
    );

    // All tasks for user paginated
    Page<ActionTask> findByUserIdOrderByTaskDateDescPriorityAsc(
            String userId, Pageable pageable
    );

    // Pending tasks for a role
    List<ActionTask> findByUserIdAndTargetRoleAndStatus(
            String userId, String targetRole, TaskStatus status
    );

    // Count completed tasks
    long countByUserIdAndStatus(String userId, TaskStatus status);
}
