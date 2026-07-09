package com.taskflow.project.dto.request;

import com.taskflow.project.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull(message = "Status is required")
    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate dueDate;
}
