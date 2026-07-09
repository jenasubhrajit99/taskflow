package com.taskflow.project.service;

import com.taskflow.common.exception.AuthorizationException;
import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.common.response.PageResponse;
import com.taskflow.project.dto.request.AddProjectMemberRequest;
import com.taskflow.project.dto.request.CreateProjectRequest;
import com.taskflow.project.dto.request.UpdateProjectRequest;
import com.taskflow.project.dto.response.ProjectMemberResponse;
import com.taskflow.project.dto.response.ProjectResponse;
import com.taskflow.project.entity.Project;
import com.taskflow.project.entity.ProjectMember;
import com.taskflow.project.entity.ProjectRole;
import com.taskflow.project.entity.ProjectStatus;
import com.taskflow.project.mapper.ProjectMapper;
import com.taskflow.project.repository.ProjectMemberRepository;
import com.taskflow.project.repository.ProjectRepository;
import com.taskflow.user.entity.User;
import com.taskflow.user.service.UserService;
import com.taskflow.workspace.entity.Workspace;
import com.taskflow.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String creatorEmail) {
        log.debug("Creating project '{}' for workspace {}", request.getName(), request.getWorkspaceId());
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", "id", request.getWorkspaceId()));
        if (projectRepository.existsByWorkspaceIdAndKey(request.getWorkspaceId(), request.getKey())) {
            throw new ConflictException("Project key '" + request.getKey() + "' already exists in this workspace");
        }
        User creator = userService.findUserEntityByEmail(creatorEmail);
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .key(request.getKey().toUpperCase())
                .workspace(workspace)
                .owner(creator)
                .status(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNING)
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .build();
        project = projectRepository.save(project);
        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(creator)
                .role(ProjectRole.OWNER)
                .build();
        projectMemberRepository.save(ownerMember);
        project.getMembers().add(ownerMember);
        log.info("Project '{}' created with key '{}' by {}", project.getName(), project.getKey(), creatorEmail);
        return projectMapper.toResponse(project);
    }

    public ProjectResponse getProject(UUID id) {
        return projectMapper.toResponse(
                projectRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id)));
    }

    public PageResponse<ProjectResponse> getWorkspaceProjects(UUID workspaceId, Pageable pageable) {
        Page<ProjectResponse> page = projectRepository.findByWorkspaceId(workspaceId, pageable)
                .map(projectMapper::toResponse);
        return PageResponse.from(page);
    }

    public List<ProjectResponse> getUserProjects(String userEmail) {
        return projectRepository.findByMemberEmail(userEmail)
                .stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request, String callerEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        verifyCallerRole(id, callerEmail, ProjectRole.OWNER, ProjectRole.MANAGER);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID id, String callerEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        verifyCallerRole(id, callerEmail, ProjectRole.OWNER);
        projectRepository.delete(project);
        log.info("Project {} deleted by {}", id, callerEmail);
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID projectId, AddProjectMemberRequest request, String callerEmail) {
        verifyCallerRole(projectId, callerEmail, ProjectRole.OWNER, ProjectRole.MANAGER);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User targetUser = userService.findUserEntityByEmail(request.getEmail());
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, targetUser.getId())) {
            throw new ConflictException("User '" + request.getEmail() + "' is already a member of this project");
        }
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(targetUser)
                .role(request.getRole())
                .build();
        return projectMapper.toMemberResponse(projectMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId, String callerEmail) {
        verifyCallerRole(projectId, callerEmail, ProjectRole.OWNER, ProjectRole.MANAGER);
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectMember", "userId", userId));
        if (member.getRole() == ProjectRole.OWNER) {
            throw new AuthorizationException("Cannot remove the project owner");
        }
        projectMemberRepository.delete(member);
    }

    public List<ProjectMemberResponse> getMembers(UUID projectId) {
        return projectMemberRepository.findByProjectId(projectId)
                .stream()
                .map(projectMapper::toMemberResponse)
                .collect(Collectors.toList());
    }

    private void verifyCallerRole(UUID projectId, String callerEmail, ProjectRole... allowedRoles) {
        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserEmail(projectId, callerEmail)
                .orElseThrow(() -> new AuthorizationException("You are not a member of this project"));
        if (Arrays.stream(allowedRoles).noneMatch(r -> r == member.getRole())) {
            throw new AuthorizationException("You do not have sufficient permissions for this action");
        }
    }
}
