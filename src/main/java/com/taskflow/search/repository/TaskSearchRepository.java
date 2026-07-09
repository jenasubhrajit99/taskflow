package com.taskflow.search.repository;

import com.taskflow.search.document.TaskDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TaskSearchRepository extends ElasticsearchRepository<TaskDocument, String> {
}
