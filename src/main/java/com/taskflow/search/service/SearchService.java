package com.taskflow.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.taskflow.common.response.PageResponse;
import com.taskflow.search.document.TaskDocument;
import com.taskflow.search.dto.response.TaskSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;

    public PageResponse<TaskSearchResponse> searchTasks(String query, String projectId,
                                                         String status, Pageable pageable) {
        try {
            SearchResponse<TaskDocument> response = elasticsearchClient.search(s -> s
                    .index("tasks")
                    .from((int) pageable.getOffset())
                    .size(pageable.getPageSize())
                    .query(q -> q
                            .bool(b -> {
                                if (query != null && !query.isBlank()) {
                                    b.must(m -> m.multiMatch(mm -> mm
                                            .fields("title^2", "description")
                                            .query(query)
                                    ));
                                }
                                if (projectId != null) {
                                    b.filter(f -> f.term(t -> t.field("projectId").value(projectId)));
                                }
                                if (status != null) {
                                    b.filter(f -> f.term(t -> t.field("status").value(status)));
                                }
                                return b;
                            })
                    ),
                    TaskDocument.class
            );

            List<TaskSearchResponse> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .map(this::toSearchResponse)
                    .toList();

            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            var page = new PageImpl<>(results, pageable, total);
            return PageResponse.from(page);

        } catch (IOException e) {
            log.error("Elasticsearch search failed: {}", e.getMessage(), e);
            var empty = new PageImpl<TaskSearchResponse>(List.of(), pageable, 0);
            return PageResponse.from(empty);
        }
    }

    private TaskSearchResponse toSearchResponse(TaskDocument doc) {
        return new TaskSearchResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getProjectId(),
                doc.getProjectName(),
                doc.getWorkspaceId(),
                doc.getAssigneeId(),
                doc.getAssigneeEmail(),
                doc.getStatus(),
                doc.getPriority(),
                doc.getCreatedAt()
        );
    }
}
