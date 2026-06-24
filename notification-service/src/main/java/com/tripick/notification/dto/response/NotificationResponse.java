package com.tripick.notification.dto.response;

import com.tripick.notification.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private final Long notificationId;
    private final String type;
    private final String title;
    private final String content;
    private final Long relatedId;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.notificationId = notification.getId();
        this.type = notification.getType().name();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.relatedId = notification.getRelatedId();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }
}
