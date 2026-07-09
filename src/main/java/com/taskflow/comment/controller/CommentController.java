package com.taskflow.comment.controller;

import com.taskflow.comment.dto.request.CreateCommentRequest;
import com.taskflow.comment.dto.request.UpdateCommentRequest;
import com.taskflow.comment.dto.response.CommentResponse;
import com.taskflow.comment.service.CommentService;
import com.taskflow.common.constant.AppConstants;
import com.taskflow.common.response.ApiResponse;
import com.taskflow.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1)
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment to a task")
    public ApiResponse<CommentResponse> createComment(@PathVariable UUID taskId,
                                                       @Valid @RequestBody CreateCommentRequest request) {
        return ApiResponse.success("Comment added successfully", commentService.createComment(taskId, request));
    }

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Get comments for a task")
    public ApiResponse<PageResponse<CommentResponse>> getComments(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ApiResponse.success(commentService.getCommentsByTask(taskId, pageable));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Update a comment")
    public ApiResponse<CommentResponse> updateComment(@PathVariable UUID commentId,
                                                       @Valid @RequestBody UpdateCommentRequest request) {
        return ApiResponse.success("Comment updated successfully", commentService.updateComment(commentId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a comment")
    public void deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
    }
}
