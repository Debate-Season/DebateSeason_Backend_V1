package com.debateseason_backend_v1.domain.chat.infrastructure.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatJpaRepository extends JpaRepository<ChatEntity,Long> {

    @Query("""
           SELECT c FROM chat c
           WHERE c.chatRoomId.id = :roomId
           AND c.id < :cursor
           AND DATE(c.timeStamp) = :date
           ORDER BY c.id DESC
           """)
    List<ChatEntity> findByRoomIdAndCursorAndDate(
            @Param("roomId") Long roomId,
            @Param("cursor") Long cursor,
            @Param("date") LocalDate date,
            Pageable pageable
    );


    @Query("""
           SELECT COUNT(c) FROM chat c
           WHERE c.chatRoomId.id = :roomId
           AND DATE(c.timeStamp) = :date
           """)
    long countByRoomIdAndDate(@Param("roomId") Long roomId, @Param("date") LocalDate date);


    @Query("""
           SELECT c FROM chat c
           WHERE c.chatRoomId.id = :roomId AND c.id < :cursor
           ORDER BY c.id
           DESC
           """)
    List<ChatEntity> findByRoomIdAndCursor(Long roomId, Long cursor, Pageable pageable);


    @Query("""
           SELECT COUNT(c)
           FROM chat c
           WHERE c.chatRoomId.id = :roomId
           """)
    int countByRoomId(Long roomId);


    // 가장 최근대화 불러오기.
    @Query("""
            SELECT c.timeStamp FROM chat c
            WHERE c.chatRoomId.id = :chatRoomId
            ORDER BY c.timeStamp
            DESC
            LIMIT 1
            """)
    Optional<LocalDateTime> findMostRecentMessageTimestampByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
