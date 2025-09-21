package com.debateseason_backend_v1.domain.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingJpaRepository extends JpaRepository<NotificationSettingEntity, Long> {
}

