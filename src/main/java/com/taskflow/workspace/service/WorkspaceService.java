package com.taskflow.workspace.service;

import com.taskflow.common.exception.AuthorizationException;
import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.user.entity.User;
import com.taskflow.user.service.UserService;
import com.taskflow.workspace.dto.request.AddWorkspaceMemberRequest;
import com.taskflow.workspace.dto.request.CreateWorkspaceRequest;
import com.taskflow.workspace.dto.request.UpdateWorkspaceRequest;
import com.taskflow.workspace.dto.response.WorkspaceMemberResponse;
import com.taskflow.workspace.dto.response.WorkspaceResponse;
import com.taskflow.workspace.entity.Workspace;
import com.taskflow.workspace.entity.WorkspaceMember;
import com.taskflow.workspace.entity.WorkspaceRole;
import com.taskflow.workspace.mapper.WorkspaceMapper;
import com.taskflow.workspace.repository.WorkspaceMemberRepository;
import com.taskflow.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserService userService;
    private final WorkspaceMapper workspaceMapper;

    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request, String ownerEmail) {
        log.debug("Creating workspace '{}' for user: {}", request.getName(), ownerEmail);
        User owner = userService.findUserEntityByEmail(ownerEmail);
        String slug = generateUniqueSlug(request.getName());
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(slug)
                .owner(owner)
                .build();
        workspace = workspaceRepository.save(workspace);
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(owner)
                .role(WorkspaceRole.OWNER)
                .build();
        workspaceMemberRepository.save(ownerMember);
        workspace.getMembers().add(ownerMember);
        log.info("Workspace '{}' created with slug '{}' by {}", workspace.getName(), slug, ownerEmail);
        return workspaceMapper.toResponse(workspace);
    }

    public WorkspaceResponse getWorkspaceBySlug(String slug) {
        Workspace workspace = workspaceRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "slug", slug));
        return workspaceMapper.toResponse(workspace);
    }

    public WorkspaceResponse getWorkspaceById(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", id));
        return workspaceMapper.toResponse(workspace);
    }

    public List<WorkspaceResponse> getUserWorkspaces(String userEmail) {
        return workspaceRepository.findByMemberEmail(userEmail)
                .stream()
                .map(workspaceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request, String callerEmail) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", id));
        verifyCallerRole(id, callerEmail, WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        return workspaceMapper.toResponse(workspaceRepository.save(workspace));
    }

    @Transactional
    public void deleteWorkspace(UUID id, String callerEmail) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", id));
        verifyCallerRole(id, callerEmail, WorkspaceRole.OWNER);
        workspaceRepository.delete(workspace);
        log.info("Workspace {} deleted by {}", id, callerEmail);
    }

    @Transactional
    public WorkspaceMemberResponse addMember(UUID workspaceId, AddWorkspaceMemberRequest request, String callerEmail) {
        verifyCallerRole(workspaceId, callerEmail, WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", workspaceId));
        User targetUser = userService.findUserEntityByEmail(request.getEmail());
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, targetUser.getId())) {
            throw new ConflictException("User '" + request.getEmail() + "' is already a member of this workspace");
        }
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(targetUser)
                .role(request.getRole())
                .build();
        return workspaceMapper.toMemberResponse(workspaceMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID userId, String callerEmail) {
        verifyCallerRole(workspaceId, callerEmail, WorkspaceRole.OWNER, WorkspaceRole.ADMIN);
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkspaceMember", "userId", userId));
        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new AuthorizationException("Cannot remove the workspace owner");
        }
        workspaceMemberRepository.delete(member);
    }

    public List<WorkspaceMemberResponse> getMembers(UUID workspaceId) {
        return workspaceMemberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(workspaceMapper::toMemberResponse)
                .collect(Collectors.toList());
    }

    private void verifyCallerRole(UUID workspaceId, String callerEmail, WorkspaceRole... allowedRoles) {
        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserEmail(workspaceId, callerEmail)
                .orElseThrow(() -> new AuthorizationException("You are not a member of this workspace"));
        boolean allowed = Arrays.asList(allowedRoles).contains(member.getRole());
        if (!allowed) {
            throw new AuthorizationException("You do not have sufficient permissions for this action");
        }
    }

    private String generateUniqueSlug(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        String slug = base;
        int attempt = 0;
        while (workspaceRepository.existsBySlug(slug)) {
            attempt++;
            slug = base + "-" + attempt;
        }
        return slug;
    }
}
