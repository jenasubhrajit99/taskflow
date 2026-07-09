package com.taskflow.task.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskEvent(
        EventType eventType,
        UUID taskId,
        String title,
        String description,
        UUID projectId,
        String projectName,
        UUID workspaceId,
        String workspaceName,
        UUID assigneeId,
        String assigneeEmail,
        String assigneeName,
        UUID reporterId,
        String reporterEmail,
        String status,
        String priority,
        LocalDateTime eventTimestamp
) {
    public enum EventType {
        CREATED, UPDATED, DELETED, ASSIGNED, STATUS_CHANGED
    }
}
