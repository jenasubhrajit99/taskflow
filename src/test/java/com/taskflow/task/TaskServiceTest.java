package com.taskflow.task;

import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.project.entity.Project;
import com.taskflow.project.repository.ProjectMemberRepository;
import com.taskflow.project.repository.ProjectRepository;
import com.taskflow.task.dto.request.CreateTaskRequest;
import com.taskflow.task.dto.response.TaskResponse;
import com.taskflow.task.entity.Task;
import com.taskflow.task.entity.TaskPriority;
import com.taskflow.task.entity.TaskStatus;
import com.taskflow.task.mapper.TaskMapper;
import com.taskflow.task.repository.LabelRepository;
import com.taskflow.task.repository.TaskRepository;
import com.taskflow.user.entity.User;
import com.taskflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock LabelRepository labelRepository;
    @Mock ProjectRepository projectRepository;
    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock UserRepository userRepository;
    @Mock TaskMapper taskMapper;
    @Mock KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    com.taskflow.task.service.TaskService taskService;

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null, Collections.emptyList())
        );
        user = new User();
        user.setEmail("user@example.com");

        project = new Project();
    }

    @Test
    void createTask_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        var request = new CreateTaskRequest("Title", null, UUID.randomUUID(), null, null, null, null, null, null, null);
        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createTask_projectNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        var request = new CreateTaskRequest("Title", null, projectId, null, null, null, null, null, null, null);
        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTask_notFound_throwsResourceNotFoundException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdWithDetails(taskId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTask(taskId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTask_notFound_throwsResourceNotFoundException() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdWithDetails(taskId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.deleteTask(taskId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
