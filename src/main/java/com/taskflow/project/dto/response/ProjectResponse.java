package com.taskflow.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.user.dto.response.UserResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponse {

    private final UUID id;
    private final String name;
    private final String description;
    private final String key;
    private final UUID workspaceId;
    private final String workspaceName;
    private final UserResponse owner;
    private final String status;
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private final int memberCount;
    private final LocalDateTime createdAt;
}
