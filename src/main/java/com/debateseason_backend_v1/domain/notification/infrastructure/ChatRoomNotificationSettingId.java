package com.debateseason_backend_v1.domain.notification.infrastructure;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatRoomNotificationSettingId implements Serializable {
    private Long userId;
    private Long chatRoomId;
}

