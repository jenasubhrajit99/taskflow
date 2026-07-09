package com.taskflow.task.dto.request;

import com.taskflow.task.entity.TaskPriority;
import com.taskflow.task.entity.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record UpdateTaskRequest(
        @Size(max = 500) String title,
        String description,
        UUID assigneeId,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Integer storyPoints,
        Set<UUID> labelIds
) {}
