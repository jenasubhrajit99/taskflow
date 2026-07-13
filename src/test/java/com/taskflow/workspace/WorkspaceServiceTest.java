package com.taskflow.workspace;

import com.taskflow.common.exception.AuthorizationException;
import com.taskflow.common.exception.ConflictException;
import com.taskflow.user.entity.User;
import com.taskflow.user.service.UserService;
import com.taskflow.workspace.dto.request.AddWorkspaceMemberRequest;
import com.taskflow.workspace.dto.request.CreateWorkspaceRequest;
import com.taskflow.workspace.entity.Workspace;
import com.taskflow.workspace.entity.WorkspaceMember;
import com.taskflow.workspace.entity.WorkspaceRole;
import com.taskflow.workspace.mapper.WorkspaceMapper;
import com.taskflow.workspace.repository.WorkspaceMemberRepository;
import com.taskflow.workspace.repository.WorkspaceRepository;
import com.taskflow.workspace.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock private UserService userService;
    @Mock private WorkspaceMapper workspaceMapper;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    void createWorkspace_savesWorkspaceAndAddsOwnerAsMember() {
        CreateWorkspaceRequest request = mock(CreateWorkspaceRequest.class);
        when(request.getName()).thenReturn("My Workspace");
        when(request.getDescription()).thenReturn("desc");
        User owner = new User();
        owner.setEmail("owner@example.com");
        when(userService.findUserEntityByEmail("owner@example.com")).thenReturn(owner);
        when(workspaceRepository.existsBySlug(anyString())).thenReturn(false);
        Workspace saved = Workspace.builder().name("My Workspace").slug("my-workspace").owner(owner).build();
        when(workspaceRepository.save(any())).thenReturn(saved);
        when(workspaceMemberRepository.save(any())).thenReturn(new WorkspaceMember());

        workspaceService.createWorkspace(request, "owner@example.com");

        verify(workspaceRepository).save(any(Workspace.class));
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void addMember_whenAlreadyMember_throwsConflictException() {
        UUID workspaceId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        User caller = new User();
        caller.setEmail("admin@example.com");
        WorkspaceMember callerMember = WorkspaceMember.builder().user(caller).role(WorkspaceRole.ADMIN).build();
        when(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, "admin@example.com"))
                .thenReturn(Optional.of(callerMember));
        User target = new User();
        target.setEmail("target@example.com");
        when(userService.findUserEntityByEmail("target@example.com")).thenReturn(target);
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(eq(workspaceId), any())).thenReturn(true);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(new Workspace()));

        AddWorkspaceMemberRequest request = mock(AddWorkspaceMemberRequest.class);
        when(request.getEmail()).thenReturn("target@example.com");
        //when(request.getRole()).thenReturn(WorkspaceRole.MEMBER);

        assertThatThrownBy(() -> workspaceService.addMember(workspaceId, request, "admin@example.com"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateWorkspace_whenNotMember_throwsAuthorizationException() {
        UUID workspaceId = UUID.randomUUID();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(new Workspace()));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, "viewer@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, mock(com.taskflow.workspace.dto.request.UpdateWorkspaceRequest.class), "viewer@example.com"))
                .isInstanceOf(AuthorizationException.class);
    }
}
