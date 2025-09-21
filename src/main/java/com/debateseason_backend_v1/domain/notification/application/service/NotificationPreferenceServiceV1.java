package com.debateseason_backend_v1.domain.notification.application.service;

import com.debateseason_backend_v1.domain.notification.infrastructure.ChatRoomNotificationSettingEntity;
import com.debateseason_backend_v1.domain.notification.infrastructure.ChatRoomNotificationSettingJpaRepository;
import com.debateseason_backend_v1.domain.notification.infrastructure.NotificationSettingEntity;
import com.debateseason_backend_v1.domain.notification.infrastructure.NotificationSettingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceV1 {

    private final NotificationSettingJpaRepository settingRepository;
    private final ChatRoomNotificationSettingJpaRepository roomSettingRepository;

    @Transactional(readOnly = true)
    public NotificationSettingEntity getOrCreateUserSetting(Long userId) {
        return settingRepository.findById(userId)
                .orElseGet(() -> settingRepository.save(NotificationSettingEntity.builder()
                        .userId(userId)
                        .chatNotificationEnabled(true)
                        .soundEnabled(true)
                        .vibrationEnabled(true)
                        .build()));
    }

    @Transactional
    public NotificationSettingEntity updateUserSetting(Long userId, boolean chatEnabled, boolean sound, boolean vibration) {
        NotificationSettingEntity setting = getOrCreateUserSetting(userId);
        setting.setChatNotificationEnabled(chatEnabled);
        setting.setSoundEnabled(sound);
        setting.setVibrationEnabled(vibration);
        return setting;
    }

    @Transactional
    public ChatRoomNotificationSettingEntity updateRoomSetting(Long userId, Long chatRoomId, boolean enabled) {
        Optional<ChatRoomNotificationSettingEntity> existing = roomSettingRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
        if (existing.isPresent()) {
            existing.get().setNotificationEnabled(enabled);
            return existing.get();
        }
        ChatRoomNotificationSettingEntity entity = ChatRoomNotificationSettingEntity.builder()
                .userId(userId)
                .chatRoomId(chatRoomId)
                .notificationEnabled(enabled)
                .build();
        return roomSettingRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public boolean isUserEligibleForChatRoom(Long userId, Long chatRoomId) {
        NotificationSettingEntity userSetting = settingRepository.findById(userId)
                .orElse(NotificationSettingEntity.builder()
                        .userId(userId)
                        .chatNotificationEnabled(true)
                        .soundEnabled(true)
                        .vibrationEnabled(true)
                        .build());
        if (!userSetting.isChatNotificationEnabled()) {
            return false;
        }
        return roomSettingRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .map(ChatRoomNotificationSettingEntity::isNotificationEnabled)
                .orElse(true);
    }
}

