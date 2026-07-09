package com.taskflow.task.repository;

import com.taskflow.task.entity.Task;
import com.taskflow.task.entity.TaskPriority;
import com.taskflow.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            """)
    Page<Task> findByProjectIdWithFilters(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("assigneeId") UUID assigneeId,
            Pageable pageable
    );

    Page<Task> findByAssigneeId(UUID assigneeId, Pageable pageable);

    @Query("SELECT t FROM Task t JOIN FETCH t.project JOIN FETCH t.reporter WHERE t.id = :id")
    Optional<Task> findByIdWithDetails(@Param("id") UUID id);

    long countByProjectIdAndStatus(UUID projectId, TaskStatus status);
}
