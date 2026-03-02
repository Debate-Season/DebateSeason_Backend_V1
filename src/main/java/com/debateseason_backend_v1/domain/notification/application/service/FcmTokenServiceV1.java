package com.debateseason_backend_v1.domain.notification.application.service;

import com.debateseason_backend_v1.domain.notification.infrastructure.FcmTokenEntity;
import com.debateseason_backend_v1.domain.notification.infrastructure.FcmTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenServiceV1 {

    private final FcmTokenJpaRepository tokenRepository;

    @Transactional
    public void upsertToken(Long userId, String deviceId, String deviceType, String fcmToken) {
        tokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                .ifPresentOrElse(existing -> {
                    existing.setFcmToken(fcmToken);
                    existing.setDeviceType(deviceType);
                    existing.setActive(true);
                    existing.setLastUsedAt(LocalDateTime.now());
                    log.info("Updated FCM token for userId={}, deviceId={}", userId, deviceId);
                }, () -> {
                    FcmTokenEntity entity = FcmTokenEntity.builder()
                            .userId(userId)
                            .deviceId(deviceId)
                            .deviceType(deviceType)
                            .fcmToken(fcmToken)
                            .active(true)
                            .lastUsedAt(LocalDateTime.now())
                            .build();
                    tokenRepository.save(entity);
                    log.info("Registered new FCM token for userId={}, deviceId={}", userId, deviceId);
                });
    }

    @Transactional
    public void deleteToken(Long userId, String deviceId) {
        tokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
        log.info("Deleted FCM token for userId={}, deviceId={}", userId, deviceId);
    }

    @Transactional(readOnly = true)
    public List<FcmTokenEntity> findActiveTokens(Long userId) {
        return tokenRepository.findByUserIdAndActiveTrue(userId);
    }
}

