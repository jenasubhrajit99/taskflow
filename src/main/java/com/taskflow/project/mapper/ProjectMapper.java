package com.taskflow.project.mapper;

import com.taskflow.project.dto.response.ProjectMemberResponse;
import com.taskflow.project.dto.response.ProjectResponse;
import com.taskflow.project.entity.Project;
import com.taskflow.project.entity.ProjectMember;
import com.taskflow.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface ProjectMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "workspaceName", source = "workspace.name")
    @Mapping(target = "status", expression = "java(project.getStatus().name())")
    @Mapping(target = "memberCount", expression = "java(project.getMembers().size())")
    ProjectResponse toResponse(Project project);

    @Mapping(target = "role", expression = "java(member.getRole().name())")
    ProjectMemberResponse toMemberResponse(ProjectMember member);
}
