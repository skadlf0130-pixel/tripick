package com.tripick.notification.dto.response;

import com.tripick.notification.entity.NotificationChannel;
import com.tripick.notification.entity.NotificationType;
import lombok.Getter;

@Getter
public class NotificationSettingResponse {

    private final NotificationType type;
    private final NotificationChannel channel;
    private final String email;

    public NotificationSettingResponse(NotificationType type, NotificationChannel channel, String email) {
        this.type = type;
        this.channel = channel;
        this.email = email;
    }
}
