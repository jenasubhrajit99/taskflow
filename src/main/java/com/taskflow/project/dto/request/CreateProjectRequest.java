package com.taskflow.project.dto.request;

import com.taskflow.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200)
    private String name;

    private String description;

    @NotBlank(message = "Project key is required")
    @Size(max = 10, message = "Project key must not exceed 10 characters")
    @Pattern(regexp = "[A-Z0-9]+", message = "Project key must contain only uppercase letters and digits")
    private String key;

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    private ProjectStatus status = ProjectStatus.PLANNING;

    private LocalDate startDate;

    private LocalDate dueDate;
}
