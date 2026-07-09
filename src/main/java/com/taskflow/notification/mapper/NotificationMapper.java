package com.taskflow.notification.mapper;

import com.taskflow.notification.dto.response.NotificationResponse;
import com.taskflow.notification.entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "recipientId", source = "recipient.id")
    @Mapping(target = "type", expression = "java(notification.getType().name())")
    NotificationResponse toResponse(Notification notification);
}
