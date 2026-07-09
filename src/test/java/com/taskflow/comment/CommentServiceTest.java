package com.taskflow.comment;

import com.taskflow.comment.dto.request.CreateCommentRequest;
import com.taskflow.comment.entity.Comment;
import com.taskflow.comment.mapper.CommentMapper;
import com.taskflow.comment.repository.CommentRepository;
import com.taskflow.comment.service.CommentService;
import com.taskflow.common.exception.AuthorizationException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.task.entity.Task;
import com.taskflow.task.repository.TaskRepository;
import com.taskflow.user.entity.User;
import com.taskflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;
    @Mock CommentMapper commentMapper;

    @InjectMocks CommentService commentService;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null, Collections.emptyList())
        );
        user = new User();
        user.setEmail("user@example.com");
    }

    @Test
    void createComment_taskNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> commentService.createComment(taskId, new CreateCommentRequest("content", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateComment_notOwner_throwsAuthorizationException() {
        User other = new User();
        other.setEmail("other@example.com");
        Comment comment = new Comment();
        comment.setAuthor(other);
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        assertThatThrownBy(() -> commentService.updateComment(commentId,
                new com.taskflow.comment.dto.request.UpdateCommentRequest("new content")))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void deleteComment_notOwner_throwsAuthorizationException() {
        User other = new User();
        other.setEmail("other@example.com");
        Comment comment = new Comment();
        comment.setAuthor(other);
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        assertThatThrownBy(() -> commentService.deleteComment(commentId))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void deleteComment_notFound_throwsResourceNotFoundException() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> commentService.deleteComment(commentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
