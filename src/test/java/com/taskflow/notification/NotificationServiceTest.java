package com.taskflow.notification;

import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.notification.entity.Notification;
import com.taskflow.notification.event.NotificationEvent;
import com.taskflow.notification.mapper.NotificationMapper;
import com.taskflow.notification.repository.NotificationRepository;
import com.taskflow.notification.service.NotificationService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationMapper notificationMapper;

    @InjectMocks NotificationService notificationService;

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
    void createFromEvent_recipientNotFound_logsWarningAndReturns() {
        UUID recipientId = UUID.randomUUID();
        when(userRepository.findById(recipientId)).thenReturn(Optional.empty());
        NotificationEvent event = new NotificationEvent(recipientId, "TASK_ASSIGNED", "Title", "Message",
                "entity-id", "TASK", LocalDateTime.now());
        notificationService.createFromEvent(event);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createFromEvent_validEvent_savesNotification() {
        UUID recipientId = UUID.randomUUID();
        user.setEmail("user@example.com");
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any())).thenReturn(new Notification());
        NotificationEvent event = new NotificationEvent(recipientId, "TASK_ASSIGNED", "Title", "Message",
                "entity-id", "TASK", LocalDateTime.now());
        notificationService.createFromEvent(event);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUnreadCount_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.getUnreadCount())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAllAsRead_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.markAllAsRead())
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
