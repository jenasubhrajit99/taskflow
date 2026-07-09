package com.taskflow.comment.mapper;

import com.taskflow.comment.dto.response.CommentResponse;
import com.taskflow.comment.entity.Comment;
import com.taskflow.user.mapper.UserMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    CommentResponse toResponse(Comment comment);
}
