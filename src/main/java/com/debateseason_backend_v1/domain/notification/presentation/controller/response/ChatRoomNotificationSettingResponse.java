package com.debateseason_backend_v1.domain.notification.presentation.controller.response;

import com.debateseason_backend_v1.domain.notification.infrastructure.ChatRoomNotificationSettingEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomNotificationSettingResponse {
    private Long chatRoomId;
    private boolean notificationEnabled;

    public static ChatRoomNotificationSettingResponse from(ChatRoomNotificationSettingEntity e) {
        return ChatRoomNotificationSettingResponse.builder()
                .chatRoomId(e.getChatRoomId())
                .notificationEnabled(e.isNotificationEnabled())
                .build();
    }
}

