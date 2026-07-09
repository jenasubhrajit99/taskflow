package com.taskflow.task.controller;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.common.response.ApiResponse;
import com.taskflow.common.response.PageResponse;
import com.taskflow.task.dto.request.CreateTaskRequest;
import com.taskflow.task.dto.request.UpdateTaskRequest;
import com.taskflow.task.dto.response.TaskResponse;
import com.taskflow.task.entity.TaskPriority;
import com.taskflow.task.entity.TaskStatus;
import com.taskflow.task.service.TaskService;
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
@RequestMapping(AppConstants.API_V1 + "/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new task")
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.success("Task created successfully", taskService.createTask(request));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by ID")
    public ApiResponse<TaskResponse> getTask(@PathVariable UUID taskId) {
        return ApiResponse.success(taskService.getTask(taskId));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project with optional filters")
    public ApiResponse<PageResponse<TaskResponse>> getTasksByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.success(taskService.getTasksByProject(projectId, status, priority, assigneeId, pageable));
    }

    @GetMapping("/my")
    @Operation(summary = "Get tasks assigned to current user")
    public ApiResponse<PageResponse<TaskResponse>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.success(taskService.getMyTasks(pageable));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task")
    public ApiResponse<TaskResponse> updateTask(@PathVariable UUID taskId,
                                                 @Valid @RequestBody UpdateTaskRequest request) {
        return ApiResponse.success("Task updated successfully", taskService.updateTask(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task")
    public void deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
    }
}
