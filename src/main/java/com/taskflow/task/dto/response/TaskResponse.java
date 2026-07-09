package com.taskflow.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.user.dto.response.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResponse(
        UUID id,
        String title,
        String description,
        UUID projectId,
        String projectName,
        UserResponse assignee,
        UserResponse reporter,
        String status,
        String priority,
        LocalDate dueDate,
        Integer storyPoints,
        UUID parentTaskId,
        Set<LabelResponse> labels,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
