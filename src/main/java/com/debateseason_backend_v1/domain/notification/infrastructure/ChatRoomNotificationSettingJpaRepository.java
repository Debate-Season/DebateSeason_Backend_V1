package com.debateseason_backend_v1.domain.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomNotificationSettingJpaRepository extends JpaRepository<ChatRoomNotificationSettingEntity, ChatRoomNotificationSettingId> {
    Optional<ChatRoomNotificationSettingEntity> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);
}

