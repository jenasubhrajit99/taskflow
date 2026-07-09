package com.taskflow.workspace.controller;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.common.response.ApiResponse;
import com.taskflow.workspace.dto.request.AddWorkspaceMemberRequest;
import com.taskflow.workspace.dto.request.CreateWorkspaceRequest;
import com.taskflow.workspace.dto.request.UpdateWorkspaceRequest;
import com.taskflow.workspace.dto.response.WorkspaceMemberResponse;
import com.taskflow.workspace.dto.response.WorkspaceResponse;
import com.taskflow.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Workspace management")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    @Operation(summary = "Get all workspaces for the current user")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getUserWorkspaces(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getUserWorkspaces(auth.getName())));
    }

    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            Authentication auth) {
        WorkspaceResponse response = workspaceService.createWorkspace(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Workspace created", response));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get workspace by slug")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getWorkspaceBySlug(slug)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Workspace updated", workspaceService.updateWorkspace(id, request, auth.getName())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workspace")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(@PathVariable UUID id, Authentication auth) {
        workspaceService.deleteWorkspace(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted"));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List workspace members")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(workspaceService.getMembers(id)));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to workspace")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddWorkspaceMemberRequest request,
            Authentication auth) {
        WorkspaceMemberResponse response = workspaceService.addMember(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Member added", response));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove a member from workspace")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Authentication auth) {
        workspaceService.removeMember(id, userId, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Member removed"));
    }
}
