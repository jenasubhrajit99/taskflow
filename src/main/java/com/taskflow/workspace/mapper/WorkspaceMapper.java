package com.taskflow.workspace.mapper;

import com.taskflow.user.mapper.UserMapper;
import com.taskflow.workspace.dto.response.WorkspaceMemberResponse;
import com.taskflow.workspace.dto.response.WorkspaceResponse;
import com.taskflow.workspace.entity.Workspace;
import com.taskflow.workspace.entity.WorkspaceMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface WorkspaceMapper {

    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "memberCount", expression = "java(workspace.getMembers().size())")
    WorkspaceResponse toResponse(Workspace workspace);

    @Mapping(target = "role", expression = "java(member.getRole().name())")
    WorkspaceMemberResponse toMemberResponse(WorkspaceMember member);
}
