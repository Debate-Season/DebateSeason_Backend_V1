package com.debateseason_backend_v1.domain.notification.presentation.controller;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.notification.application.service.NotificationPreferenceServiceV1;
import com.debateseason_backend_v1.domain.notification.infrastructure.ChatRoomNotificationSettingEntity;
import com.debateseason_backend_v1.domain.notification.infrastructure.NotificationSettingEntity;
import com.debateseason_backend_v1.domain.notification.presentation.controller.response.ChatRoomNotificationSettingResponse;
import com.debateseason_backend_v1.domain.notification.presentation.controller.response.NotificationSettingResponse;
import com.debateseason_backend_v1.domain.notification.presentation.controller.request.NotificationSettingsUpdateRequest;
import com.debateseason_backend_v1.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationSettingsControllerV1 {

    private final NotificationPreferenceServiceV1 preferenceService;

    @Operation(summary = "사용자 알림 설정 조회")
    @GetMapping("/settings")
    public ApiResult<NotificationSettingResponse> getSettings(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        NotificationSettingEntity entity = preferenceService.getOrCreateUserSetting(user.getUserId());
        return ApiResult.success("알림 설정 조회 성공", NotificationSettingResponse.from(entity));
    }

    @Operation(summary = "사용자 알림 설정 갱신")
    @PutMapping("/settings")
    public ApiResult<NotificationSettingResponse> updateSettings(
            @Valid @RequestBody NotificationSettingsUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        NotificationSettingEntity updated = preferenceService.updateUserSetting(
                user.getUserId(),
                request.getChatNotificationEnabled(),
                request.getSoundEnabled(),
                request.getVibrationEnabled());
        return ApiResult.success("알림 설정이 갱신되었습니다.", NotificationSettingResponse.from(updated));
    }

    @Operation(summary = "채팅방별 알림 설정 갱신")
    @PutMapping("/chatrooms/{chatRoomId}/settings")
    public ApiResult<ChatRoomNotificationSettingResponse> updateRoomSettings(
            @PathVariable Long chatRoomId,
            @RequestParam("enabled") boolean enabled,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ChatRoomNotificationSettingEntity updated = preferenceService.updateRoomSetting(user.getUserId(), chatRoomId, enabled);
        return ApiResult.success("채팅방 알림 설정이 갱신되었습니다.", ChatRoomNotificationSettingResponse.from(updated));
    }
}
