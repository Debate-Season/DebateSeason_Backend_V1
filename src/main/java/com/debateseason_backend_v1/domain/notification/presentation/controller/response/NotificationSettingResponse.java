package com.debateseason_backend_v1.domain.notification.presentation.controller.response;

import com.debateseason_backend_v1.domain.notification.infrastructure.NotificationSettingEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSettingResponse {
    private boolean chatNotificationEnabled;
    private boolean soundEnabled;
    private boolean vibrationEnabled;

    public static NotificationSettingResponse from(NotificationSettingEntity e) {
        return NotificationSettingResponse.builder()
                .chatNotificationEnabled(e.isChatNotificationEnabled())
                .soundEnabled(e.isSoundEnabled())
                .vibrationEnabled(e.isVibrationEnabled())
                .build();
    }
}

