package com.tripick.notification.repository;

import com.tripick.notification.entity.NotificationSetting;
import com.tripick.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUserIdAndType(Long userId, NotificationType type);

    List<NotificationSetting> findByUserId(Long userId);
}
