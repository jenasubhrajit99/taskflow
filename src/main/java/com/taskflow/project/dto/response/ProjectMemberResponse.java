package com.taskflow.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.user.dto.response.UserResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectMemberResponse {

    private final UUID id;
    private final UserResponse user;
    private final String role;
    private final LocalDateTime createdAt;
}
