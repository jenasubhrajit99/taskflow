package com.taskflow.project.controller;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.common.response.ApiResponse;
import com.taskflow.common.response.PageResponse;
import com.taskflow.project.dto.request.AddProjectMemberRequest;
import com.taskflow.project.dto.request.CreateProjectRequest;
import com.taskflow.project.dto.request.UpdateProjectRequest;
import com.taskflow.project.dto.response.ProjectMemberResponse;
import com.taskflow.project.dto.response.ProjectResponse;
import com.taskflow.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects for the current user")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getUserProjects(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getUserProjects(auth.getName())));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication auth) {
        ProjectResponse response = projectService.createProject(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Project created", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProject(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.updateProject(id, request, auth.getName())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID id, Authentication auth) {
        projectService.deleteProject(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Project deleted"));
    }

    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get all projects in a workspace")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getWorkspaceProjects(
            @PathVariable UUID workspaceId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getWorkspaceProjects(workspaceId, pageable)));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List project members")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getMembers(id)));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddProjectMemberRequest request,
            Authentication auth) {
        ProjectMemberResponse response = projectService.addMember(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Member added", response));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from project")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Authentication auth) {
        projectService.removeMember(id, userId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Member removed"));
    }
}
