package com.taskflow.workspace.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.user.dto.response.UserResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspaceResponse {

    private final UUID id;
    private final String name;
    private final String description;
    private final String slug;
    private final UserResponse owner;
    private final int memberCount;
    private final LocalDateTime createdAt;
}
