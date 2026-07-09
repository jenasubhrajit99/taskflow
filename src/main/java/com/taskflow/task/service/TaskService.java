package com.taskflow.task.service;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.common.exception.AuthorizationException;
import com.taskflow.common.exception.ConflictException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.common.response.PageResponse;
import com.taskflow.common.util.SecurityUtils;
import com.taskflow.project.entity.Project;
import com.taskflow.project.entity.ProjectMember;
import com.taskflow.project.repository.ProjectMemberRepository;
import com.taskflow.project.repository.ProjectRepository;
import com.taskflow.task.dto.request.CreateLabelRequest;
import com.taskflow.task.dto.request.CreateTaskRequest;
import com.taskflow.task.dto.request.UpdateTaskRequest;
import com.taskflow.task.dto.response.LabelResponse;
import com.taskflow.task.dto.response.TaskResponse;
import com.taskflow.task.entity.Label;
import com.taskflow.task.entity.Task;
import com.taskflow.task.entity.TaskPriority;
import com.taskflow.task.entity.TaskStatus;
import com.taskflow.task.event.TaskEvent;
import com.taskflow.task.mapper.TaskMapper;
import com.taskflow.task.repository.LabelRepository;
import com.taskflow.task.repository.TaskRepository;
import com.taskflow.user.entity.User;
import com.taskflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail().orElseThrow(() -> new AuthorizationException("Invalid User"));
        User reporter = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentUserEmail));

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));

        requireProjectAccess(project.getId(), currentUserEmail);

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setProject(project);
        task.setReporter(reporter);
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setDueDate(request.dueDate());
        task.setStoryPoints(request.storyPoints());

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
            task.setAssignee(assignee);
        }

        if (request.parentTaskId() != null) {
            Task parent = taskRepository.findById(request.parentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", request.parentTaskId()));
            task.setParentTask(parent);
        }

        if (request.labelIds() != null && !request.labelIds().isEmpty()) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(request.labelIds()));
            task.setLabels(labels);
        }

        Task saved = taskRepository.save(task);
        publishTaskEvent(saved, TaskEvent.EventType.CREATED);
        log.info("Task created: {} in project: {}", saved.getId(), project.getId());
        return taskMapper.toResponse(saved);
    }

    public TaskResponse getTask(UUID taskId) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        requireProjectAccess(task.getProject().getId(), SecurityUtils.getCurrentUserEmail().orElse(""));
        return taskMapper.toResponse(task);
    }

    public PageResponse<TaskResponse> getTasksByProject(UUID projectId, TaskStatus status,
                                                         TaskPriority priority, UUID assigneeId,
                                                         Pageable pageable) {
        String email = SecurityUtils.getCurrentUserEmail().orElseThrow(() -> new AuthorizationException("Invalid User"));
        requireProjectAccess(projectId, email);
        var page = taskRepository.findByProjectIdWithFilters(projectId, status, priority, assigneeId, pageable);
        return PageResponse.from(page.map(taskMapper::toResponse));
    }

    public PageResponse<TaskResponse> getMyTasks(Pageable pageable) {
        String email = SecurityUtils.getCurrentUserEmail().orElseThrow(() -> new AuthorizationException("Invalid User"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        var page = taskRepository.findByAssigneeId(user.getId(), pageable);
        return PageResponse.from(page.map(taskMapper::toResponse));
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        requireProjectAccess(task.getProject().getId(), SecurityUtils.getCurrentUserEmail().orElse(""));

        TaskStatus oldStatus = task.getStatus();

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.status() != null) task.setStatus(request.status());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.storyPoints() != null) task.setStoryPoints(request.storyPoints());

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
            task.setAssignee(assignee);
        }

        if (request.labelIds() != null) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(request.labelIds()));
            task.setLabels(labels);
        }

        Task updated = taskRepository.save(task);

        TaskEvent.EventType eventType = (request.status() != null && !request.status().equals(oldStatus))
                ? TaskEvent.EventType.STATUS_CHANGED
                : TaskEvent.EventType.UPDATED;
        publishTaskEvent(updated, eventType);
        return taskMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTask(UUID taskId) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        requireProjectAccess(task.getProject().getId(), SecurityUtils.getCurrentUserEmail().orElse(""));
        publishTaskEvent(task, TaskEvent.EventType.DELETED);
        taskRepository.delete(task);
        log.info("Task deleted: {}", taskId);
    }

    public List<LabelResponse> getLabelsByProject(UUID projectId) {
        return labelRepository.findByProjectId(projectId)
                .stream()
                .map(taskMapper::toLabelResponse)
                .toList();
    }

    @Transactional
    public LabelResponse createLabel(UUID projectId, CreateLabelRequest request) {
        if (labelRepository.existsByProjectIdAndName(projectId, request.name())) {
            throw new ConflictException("Label with name '" + request.name() + "' already exists in this project");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        Label label = new Label(request.name(), request.color() != null ? request.color() : "#6366f1", project);
        return taskMapper.toLabelResponse(labelRepository.save(label));
    }

    @Transactional
    public void deleteLabel(UUID labelId) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label", "id", labelId));
        labelRepository.delete(label);
    }

    private void requireProjectAccess(UUID projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());
        boolean isOwner = projectRepository.findById(projectId)
                .map(p -> p.getOwner().getId().equals(user.getId()))
                .orElse(false);
        if (!isMember && !isOwner) {
            throw new AuthorizationException("You do not have access to this project");
        }
    }

    private void publishTaskEvent(Task task, TaskEvent.EventType eventType) {
        try {
            TaskEvent event = new TaskEvent(
                    eventType,
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getProject().getId(),
                    task.getProject().getName(),
                    task.getProject().getWorkspace().getId(),
                    task.getProject().getWorkspace().getName(),
                    task.getAssignee() != null ? task.getAssignee().getId() : null,
                    task.getAssignee() != null ? task.getAssignee().getEmail() : null,
                    task.getAssignee() != null ? task.getAssignee().getCreatedBy() : null,
                    task.getReporter().getId(),
                    task.getReporter().getEmail(),
                    task.getStatus().name(),
                    task.getPriority().name(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send(AppConstants.Topics.TASK_EVENTS, task.getId().toString(), event);
        } catch (Exception e) {
            log.warn("Failed to publish task event for task {}: {}", task.getId(), e.getMessage());
        }
    }
}
