package com.taskflow.notification.consumer;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.notification.event.NotificationEvent;
import com.taskflow.notification.service.NotificationService;
import com.taskflow.task.event.TaskEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = AppConstants.Topics.TASK_EVENTS,
            groupId = "notification-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TaskEvent event, Acknowledgment ack) {
        try {
            log.debug("Received task event: {} for task {}", event.eventType(), event.taskId());
            processEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing task event for task {}: {}", event.taskId(), e.getMessage(), e);
            ack.acknowledge();
        }
    }

    private void processEvent(TaskEvent event) {
        switch (event.eventType()) {
            case ASSIGNED -> {
                if (event.assigneeId() != null) {
                    notificationService.createFromEvent(new NotificationEvent(
                            event.assigneeId(),
                            "TASK_ASSIGNED",
                            "Task Assigned: " + event.title(),
                            "You have been assigned to task \"" + event.title() + "\" in project " + event.projectName(),
                            event.taskId().toString(),
                            "TASK",
                            LocalDateTime.now()
                    ));
                }
            }
            case STATUS_CHANGED -> {
                if (event.assigneeId() != null) {
                    notificationService.createFromEvent(new NotificationEvent(
                            event.assigneeId(),
                            "TASK_STATUS_CHANGED",
                            "Task Status Updated: " + event.title(),
                            "Task \"" + event.title() + "\" status changed to " + event.status(),
                            event.taskId().toString(),
                            "TASK",
                            LocalDateTime.now()
                    ));
                }
            }
            default -> log.debug("No notification needed for event type: {}", event.eventType());
        }
    }
}
