package com.taskflow.notification.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationEvent(
        UUID recipientId,
        String type,
        String title,
        String message,
        String relatedEntityId,
        String relatedEntityType,
        LocalDateTime eventTimestamp
) {}
