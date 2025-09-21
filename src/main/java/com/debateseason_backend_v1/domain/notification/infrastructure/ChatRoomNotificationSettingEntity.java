package com.debateseason_backend_v1.domain.notification.infrastructure;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(ChatRoomNotificationSettingId.class)
@Table(name = "chatroom_notification_setting")
public class ChatRoomNotificationSettingEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled = true;
}

