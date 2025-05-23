package com.debateseason_backend_v1.domain.chat.infrastructure.chat;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Repository
public class ChatRepositoryImpl implements ChatRepository {

    private final ChatJpaRepository chatJpaRepository;


    @Override
    public ChatEntity save(ChatEntity chat) {
        return chatJpaRepository.save(chat);
    }

    @Override
    public List<ChatEntity> findByRoomIdAndCursorAndDate(Long roomId, Long cursor, LocalDate date, Pageable pageable) {
        return chatJpaRepository.findByRoomIdAndCursorAndDate(roomId,cursor,date,pageable);
    }

    @Override
    public long countByRoomIdAndDate(Long roomId, LocalDate date) {
        return chatJpaRepository.countByRoomIdAndDate(roomId,date);
    }

    @Override
    public List<ChatEntity> findByRoomIdAndCursor(Long roomId, Long cursor, Pageable pageable) {
        return chatJpaRepository.findByRoomIdAndCursor(roomId,cursor,pageable);
    }

    @Override
    public int countByRoomId(Long roomId) {
        return chatJpaRepository.countByRoomId(roomId);
    }

    @Override
    public ChatEntity findById(Long id) {
        return chatJpaRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MESSAGE));
    }

    @Override
    public Optional<LocalDateTime> findMostRecentMessageTimestampByChatRoomId(Long chatRoomId) {
        return chatJpaRepository.findMostRecentMessageTimestampByChatRoomId(chatRoomId);
    }
}

