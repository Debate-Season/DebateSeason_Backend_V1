package com.debateseason_backend_v1.domain.notification.infrastructure;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_setting")
public class NotificationSettingEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "chat_notification_enabled", nullable = false)
    private boolean chatNotificationEnabled = true;

    @Column(name = "sound_enabled", nullable = false)
    private boolean soundEnabled = true;

    @Column(name = "vibration_enabled", nullable = false)
    private boolean vibrationEnabled = true;
}

