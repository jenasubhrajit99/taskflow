package com.taskflow.search.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskSearchResponse(
        String id,
        String title,
        String description,
        String projectId,
        String projectName,
        String workspaceId,
        String assigneeId,
        String assigneeEmail,
        String status,
        String priority,
        LocalDateTime createdAt
) {}
