package com.taskflow.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String role;
    private final String status;
    private final boolean emailVerified;
    private final String profilePictureUrl;
    private final LocalDateTime createdAt;
}
