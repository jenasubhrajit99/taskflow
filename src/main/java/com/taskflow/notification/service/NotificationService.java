package com.taskflow.notification.service;

import com.taskflow.common.exception.AuthorizationException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.common.response.PageResponse;
import com.taskflow.common.util.SecurityUtils;
import com.taskflow.notification.dto.response.NotificationResponse;
import com.taskflow.notification.entity.Notification;
import com.taskflow.notification.entity.NotificationType;
import com.taskflow.notification.event.NotificationEvent;
import com.taskflow.notification.mapper.NotificationMapper;
import com.taskflow.notification.repository.NotificationRepository;
import com.taskflow.user.entity.User;
import com.taskflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void createFromEvent(NotificationEvent event) {
        User recipient = userRepository.findById(event.recipientId()).orElse(null);
        if (recipient == null) {
            log.warn("Cannot create notification: recipient {} not found", event.recipientId());
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(NotificationType.valueOf(event.type()));
        notification.setTitle(event.title());
        notification.setMessage(event.message());
        notification.setRelatedEntityId(event.relatedEntityId());
        notification.setRelatedEntityType(event.relatedEntityType());

        notificationRepository.save(notification);
        log.debug("Notification created for user {}: {}", recipient.getEmail(), event.title());
    }

    public PageResponse<NotificationResponse> getMyNotifications(boolean unreadOnly, Pageable pageable) {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new AuthorizationException("Invalid User"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Page<Notification> page = unreadOnly
                ? notificationRepository.findByRecipientIdAndRead(user.getId(), false, pageable)
                : notificationRepository.findByRecipientId(user.getId(), pageable);
        return PageResponse.from(page.map(notificationMapper::toResponse));
    }

    public long getUnreadCount() {
        String email = SecurityUtils.getCurrentUserEmail().orElseThrow(() -> new AuthorizationException("Invalid User"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return notificationRepository.countByRecipientIdAndRead(user.getId(), false);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        String email = SecurityUtils.getCurrentUserEmail().orElseThrow(() -> new AuthorizationException("Invalid User"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new AuthorizationException("You can only mark your own notifications as read");
        }

        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead() {
        String email = SecurityUtils.getCurrentUserEmail().orElseThrow(() -> new AuthorizationException("Invalid User"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return notificationRepository.markAllAsRead(user.getId());
    }
}
