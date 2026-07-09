package com.taskflow.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.user.dto.response.UserResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommentResponse(
        UUID id,
        String content,
        UUID taskId,
        UserResponse author,
        UUID parentCommentId,
        boolean edited,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
