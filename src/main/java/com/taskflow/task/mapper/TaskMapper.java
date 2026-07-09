package com.taskflow.task.mapper;

import com.taskflow.task.dto.response.LabelResponse;
import com.taskflow.task.dto.response.TaskResponse;
import com.taskflow.task.entity.Label;
import com.taskflow.task.entity.Task;
import com.taskflow.user.mapper.UserMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "status", expression = "java(task.getStatus().name())")
    @Mapping(target = "priority", expression = "java(task.getPriority().name())")
    @Mapping(target = "parentTaskId", source = "parentTask.id")
    TaskResponse toResponse(Task task);

    @Mapping(target = "projectId", source = "project.id")
    LabelResponse toLabelResponse(Label label);
}
