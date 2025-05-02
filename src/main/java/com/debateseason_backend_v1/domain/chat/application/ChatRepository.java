package com.debateseason_backend_v1.domain.chat.application;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat.Chat;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRepository {

    Chat save(Chat chat);

    List<Chat> findByRoomIdAndCursorAndDate(Long roomId, Long cursor, LocalDate date, Pageable pageable);

    long countByRoomIdAndDate(Long roomId,LocalDate date);

    List<Chat> findByRoomIdAndCursor(Long roomId, Long cursor, Pageable pageable);

    int countByRoomId(Long roomId);

    // 가장 최근 대화 불러오기.
    Optional<LocalDateTime> findMostRecentMessageTimestampByChatRoomId(Long chatRoomId);
}
