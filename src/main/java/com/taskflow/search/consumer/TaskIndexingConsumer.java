package com.taskflow.search.consumer;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.search.document.TaskDocument;
import com.taskflow.search.repository.TaskSearchRepository;
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
public class TaskIndexingConsumer {

    private final TaskSearchRepository taskSearchRepository;

    @KafkaListener(
            topics = AppConstants.Topics.TASK_EVENTS,
            groupId = "search-indexing-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TaskEvent event, Acknowledgment ack) {
        try {
            log.debug("Indexing task event: {} for task {}", event.eventType(), event.taskId());
            handleEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error indexing task {}: {}", event.taskId(), e.getMessage(), e);
            ack.acknowledge();
        }
    }

    private void handleEvent(TaskEvent event) {
        switch (event.eventType()) {
            case CREATED, UPDATED, ASSIGNED, STATUS_CHANGED -> indexTask(event);
            case DELETED -> {
                taskSearchRepository.deleteById(event.taskId().toString());
                log.debug("Task {} removed from search index", event.taskId());
            }
        }
    }

    private void indexTask(TaskEvent event) {
        TaskDocument doc = new TaskDocument();
        doc.setId(event.taskId().toString());
        doc.setTitle(event.title());
        doc.setDescription(event.description());
        doc.setProjectId(event.projectId() != null ? event.projectId().toString() : null);
        doc.setProjectName(event.projectName());
        doc.setWorkspaceId(event.workspaceId() != null ? event.workspaceId().toString() : null);
        doc.setAssigneeId(event.assigneeId() != null ? event.assigneeId().toString() : null);
        doc.setAssigneeEmail(event.assigneeEmail());
        doc.setStatus(event.status());
        doc.setPriority(event.priority());
        doc.setUpdatedAt(LocalDateTime.now());

        taskSearchRepository.save(doc);
        log.debug("Task {} indexed in Elasticsearch", event.taskId());
    }
}
