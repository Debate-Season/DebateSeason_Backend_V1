package com.debateseason_backend_v1.domain.notification.presentation.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationSettingsUpdateRequest {
    @NotNull
    private Boolean chatNotificationEnabled;
    @NotNull
    private Boolean soundEnabled;
    @NotNull
    private Boolean vibrationEnabled;
}

