package com.taskflow.task.dto.request;

import com.taskflow.task.entity.TaskPriority;
import com.taskflow.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank @Size(max = 500) String title,
        String description,
        @NotNull UUID projectId,
        UUID assigneeId,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Integer storyPoints,
        UUID parentTaskId,
        Set<UUID> labelIds
) {}
