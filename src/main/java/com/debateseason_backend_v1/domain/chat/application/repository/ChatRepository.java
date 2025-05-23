package com.debateseason_backend_v1.domain.chat.application.repository;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRepository {

    ChatEntity save(ChatEntity chat);

    List<ChatEntity> findByRoomIdAndCursorAndDate(Long roomId, Long cursor, LocalDate date, Pageable pageable);

    long countByRoomIdAndDate(Long roomId,LocalDate date);

    List<ChatEntity> findByRoomIdAndCursor(Long roomId, Long cursor, Pageable pageable);

    int countByRoomId(Long roomId);

    ChatEntity findById(Long id);

    // 가장 최근 대화 불러오기.
    Optional<LocalDateTime> findMostRecentMessageTimestampByChatRoomId(Long chatRoomId);
}
