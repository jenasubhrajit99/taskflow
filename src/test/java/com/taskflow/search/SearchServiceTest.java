package com.taskflow.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.taskflow.common.response.PageResponse;
import com.taskflow.search.dto.response.TaskSearchResponse;
import com.taskflow.search.service.SearchService;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock ElasticsearchClient elasticsearchClient;

    @InjectMocks SearchService searchService;

    @Test
    void searchTasks_elasticsearchThrows_returnsEmptyPage() throws IOException {
    	when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenThrow(new IOException("connection refused"));
    	PageResponse<TaskSearchResponse> result = searchService.searchTasks("test", null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
