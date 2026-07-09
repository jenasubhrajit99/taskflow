package com.taskflow.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(
        UUID id,
        UUID recipientId,
        String type,
        String title,
        String message,
        boolean read,
        String relatedEntityId,
        String relatedEntityType,
        LocalDateTime createdAt
) {}
