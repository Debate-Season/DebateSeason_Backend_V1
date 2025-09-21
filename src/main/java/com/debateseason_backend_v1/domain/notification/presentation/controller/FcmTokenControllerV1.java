package com.debateseason_backend_v1.domain.notification.presentation.controller;

import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.notification.application.service.FcmTokenServiceV1;
import com.debateseason_backend_v1.domain.notification.presentation.controller.request.FcmTokenRequest;
import com.debateseason_backend_v1.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
public class FcmTokenControllerV1 {

    private final FcmTokenServiceV1 tokenService;

    @Operation(summary = "FCM 토큰 등록/갱신")
    @PostMapping("/token")
    public VoidApiResult registerToken(
            @Valid @RequestBody FcmTokenRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        tokenService.upsertToken(user.getUserId(), request.getDeviceId(), request.getDeviceType(), request.getFcmToken());
        return VoidApiResult.success("FCM 토큰이 등록되었습니다.");
    }

    @Operation(summary = "FCM 토큰 갱신")
    @PutMapping("/token")
    public VoidApiResult updateToken(
            @Valid @RequestBody FcmTokenRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        tokenService.upsertToken(user.getUserId(), request.getDeviceId(), request.getDeviceType(), request.getFcmToken());
        return VoidApiResult.success("FCM 토큰이 갱신되었습니다.");
    }

    @Operation(summary = "FCM 토큰 삭제")
    @DeleteMapping("/token")
    public VoidApiResult deleteToken(
            @RequestParam("deviceId") String deviceId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        tokenService.deleteToken(user.getUserId(), deviceId);
        return VoidApiResult.success("FCM 토큰이 삭제되었습니다.");
    }
}

