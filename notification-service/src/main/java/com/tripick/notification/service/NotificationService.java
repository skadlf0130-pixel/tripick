package com.tripick.notification.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.notification.entity.Notification;
import com.tripick.notification.entity.NotificationChannel;
import com.tripick.notification.entity.NotificationType;
import com.tripick.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingService notificationSettingService;
    private final MailService mailService;

    @Transactional
    public void createFromEvent(Long userId, NotificationType type, String title, String content, Long relatedId) {
        NotificationChannel channel = notificationSettingService.getChannel(userId, type);
        if (channel == NotificationChannel.OFF) {
            return;
        }

        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .build());

        if (channel == NotificationChannel.EMAIL) {
            String email = notificationSettingService.getEmail(userId, type);
            if (email != null) {
                mailService.send(email, title, content);
            }
        }
    }

    public Page<Notification> getPage(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new TripickException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.isOwner(userId)) {
            throw new TripickException(ErrorCode.FORBIDDEN);
        }
        notification.markRead();
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findByUserIdAndIsReadFalse(userId)
                .forEach(Notification::markRead);
    }
}
