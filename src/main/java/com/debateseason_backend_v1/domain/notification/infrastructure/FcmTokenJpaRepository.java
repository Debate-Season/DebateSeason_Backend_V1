package com.debateseason_backend_v1.domain.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenJpaRepository extends JpaRepository<FcmTokenEntity, Long> {
    List<FcmTokenEntity> findByUserIdAndActiveTrue(Long userId);
    Optional<FcmTokenEntity> findByUserIdAndDeviceId(Long userId, String deviceId);
    void deleteByUserIdAndDeviceId(Long userId, String deviceId);
}

