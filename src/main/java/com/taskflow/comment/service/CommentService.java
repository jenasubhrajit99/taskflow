package com.taskflow.comment.service;

import com.taskflow.comment.dto.request.CreateCommentRequest;
import com.taskflow.comment.dto.request.UpdateCommentRequest;
import com.taskflow.comment.dto.response.CommentResponse;
import com.taskflow.comment.entity.Comment;
import com.taskflow.comment.mapper.CommentMapper;
import com.taskflow.comment.repository.CommentRepository;
import com.taskflow.common.exception.AuthorizationException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.common.response.PageResponse;
import com.taskflow.common.util.SecurityUtils;
import com.taskflow.task.entity.Task;
import com.taskflow.task.repository.TaskRepository;
import com.taskflow.user.entity.User;
import com.taskflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request) {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new AuthorizationException("Invalid User"));
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setTask(task);
        comment.setAuthor(author);

        if (request.parentCommentId() != null) {
            Comment parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.parentCommentId()));
            comment.setParentComment(parent);
        }

        Comment saved = commentRepository.save(comment);
        log.info("Comment created on task {} by {}", taskId, email);
        return commentMapper.toResponse(saved);
    }

    public PageResponse<CommentResponse> getCommentsByTask(UUID taskId, Pageable pageable) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        var page = commentRepository.findTopLevelByTaskId(taskId, pageable);
        return PageResponse.from(page.map(commentMapper::toResponse));
    }

    @Transactional
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest request) {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new AuthorizationException("Invalid User"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new AuthorizationException("You can only edit your own comments");
        }

        comment.setContent(request.content());
        comment.setEdited(true);
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID commentId) {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new AuthorizationException("Invalid User"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getEmail().equals(email)) {
            throw new AuthorizationException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
        log.info("Comment {} deleted by {}", commentId, email);
    }
}
