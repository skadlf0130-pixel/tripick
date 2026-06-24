package com.tripick.notification.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.notification.entity.NotificationChannel;
import com.tripick.notification.entity.NotificationSetting;
import com.tripick.notification.entity.NotificationType;
import com.tripick.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    public static final NotificationChannel DEFAULT_CHANNEL = NotificationChannel.PUSH;

    private final NotificationSettingRepository notificationSettingRepository;

    public List<NotificationSetting> getAll(Long userId) {
        Map<NotificationType, NotificationSetting> saved = notificationSettingRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(NotificationSetting::getType, s -> s));

        return Arrays.stream(NotificationType.values())
                .map(type -> saved.getOrDefault(type,
                        NotificationSetting.builder().userId(userId).type(type).channel(DEFAULT_CHANNEL).build()))
                .toList();
    }

    public NotificationChannel getChannel(Long userId, NotificationType type) {
        return notificationSettingRepository.findByUserIdAndType(userId, type)
                .map(NotificationSetting::getChannel)
                .orElse(DEFAULT_CHANNEL);
    }

    public String getEmail(Long userId, NotificationType type) {
        return notificationSettingRepository.findByUserIdAndType(userId, type)
                .map(NotificationSetting::getEmail)
                .orElse(null);
    }

    @Transactional
    public NotificationSetting update(Long userId, NotificationType type, NotificationChannel channel, String email) {
        if (channel == NotificationChannel.EMAIL && (email == null || email.isBlank())) {
            throw new TripickException(ErrorCode.INVALID_PARAMETER);
        }

        return notificationSettingRepository.findByUserIdAndType(userId, type)
                .map(setting -> {
                    setting.change(channel, email);
                    return setting;
                })
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.builder()
                                .userId(userId)
                                .type(type)
                                .channel(channel)
                                .email(email)
                                .build()));
    }
}
