package com.taskflow.search.controller;

import com.taskflow.common.constant.AppConstants;
import com.taskflow.common.response.ApiResponse;
import com.taskflow.common.response.PageResponse;
import com.taskflow.search.dto.response.TaskSearchResponse;
import com.taskflow.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_V1 + "/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text search endpoints")
@SecurityRequirement(name = "BearerAuth")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/tasks")
    @Operation(summary = "Search tasks using full-text search")
    public ApiResponse<PageResponse<TaskSearchResponse>> searchTasks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        return ApiResponse.success(searchService.searchTasks(query, projectId, status, pageable));
    }
}
