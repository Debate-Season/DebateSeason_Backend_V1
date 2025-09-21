package com.debateseason_backend_v1.domain.notification.presentation.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenRequest {
    @NotBlank
    @Size(max = 4096)
    private String fcmToken;

    @NotBlank
    private String deviceId;

    @NotBlank
    private String deviceType; // android, ios
}

