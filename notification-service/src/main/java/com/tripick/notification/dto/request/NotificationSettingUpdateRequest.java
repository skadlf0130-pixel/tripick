package com.tripick.notification.dto.request;

import com.tripick.notification.entity.NotificationChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationSettingUpdateRequest {

    @NotNull(message = "수신 채널을 선택해주세요")
    private NotificationChannel channel;

    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;
}
