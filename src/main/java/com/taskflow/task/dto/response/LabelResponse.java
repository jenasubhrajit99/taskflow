package com.taskflow.task.dto.response;

import java.util.UUID;

public record LabelResponse(
        UUID id,
        String name,
        String color,
        UUID projectId
) {}
