package com.taskflow.project;

import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.project.dto.request.CreateProjectRequest;
import com.taskflow.project.entity.Project;
import com.taskflow.project.entity.ProjectRole;
import com.taskflow.project.entity.ProjectStatus;
import com.taskflow.project.mapper.ProjectMapper;
import com.taskflow.project.repository.ProjectMemberRepository;
import com.taskflow.project.repository.ProjectRepository;
import com.taskflow.project.service.ProjectService;
import com.taskflow.user.entity.User;
import com.taskflow.user.service.UserService;
import com.taskflow.workspace.entity.Workspace;
import com.taskflow.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private UserService userService;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_whenKeyDuplicate_throwsConflictException() {
        UUID workspaceId = UUID.randomUUID();
        CreateProjectRequest request = mock(CreateProjectRequest.class);
        when(request.getWorkspaceId()).thenReturn(workspaceId);
        when(request.getKey()).thenReturn("TF");
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(new Workspace()));
        when(projectRepository.existsByWorkspaceIdAndKey(workspaceId, "TF")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request, "user@example.com"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createProject_savesProjectAndAddsMemberAsOwner() {
        UUID workspaceId = UUID.randomUUID();
        CreateProjectRequest request = mock(CreateProjectRequest.class);
        when(request.getWorkspaceId()).thenReturn(workspaceId);
        when(request.getKey()).thenReturn("TF");
        when(request.getName()).thenReturn("TaskFlow");
        when(request.getStatus()).thenReturn(ProjectStatus.PLANNING);
        Workspace ws = new Workspace();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(ws));
        when(projectRepository.existsByWorkspaceIdAndKey(workspaceId, "TF")).thenReturn(false);
        User user = new User();
        when(userService.findUserEntityByEmail("user@example.com")).thenReturn(user);
        Project saved = new Project();
        when(projectRepository.save(any())).thenReturn(saved);
        when(projectMemberRepository.save(any())).thenReturn(null);

        projectService.createProject(request, "user@example.com");

        verify(projectRepository).save(any(Project.class));
        verify(projectMemberRepository).save(any());
    }

    @Test
    void getProject_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project");
    }
}
