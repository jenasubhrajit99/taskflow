package com.taskflow.task.controller;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.common.response.ApiResponse;
import com.taskflow.task.dto.request.CreateLabelRequest;
import com.taskflow.task.dto.response.LabelResponse;
import com.taskflow.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/projects/{projectId}/labels")
@RequiredArgsConstructor
@Tag(name = "Labels", description = "Label management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class LabelController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Get all labels for a project")
    public ApiResponse<List<LabelResponse>> getLabelsByProject(@PathVariable UUID projectId) {
        return ApiResponse.success(taskService.getLabelsByProject(projectId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a label for a project")
    public ApiResponse<LabelResponse> createLabel(@PathVariable UUID projectId,
                                                   @Valid @RequestBody CreateLabelRequest request) {
        return ApiResponse.success("Label created successfully", taskService.createLabel(projectId, request));
    }

    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a label")
    public void deleteLabel(@PathVariable UUID projectId, @PathVariable UUID labelId) {
        taskService.deleteLabel(labelId);
    }
}
