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


    // ---- v1.3.5 스레드 통합: 스레드 단위 조회 ----

    // 구 앱 히스토리 보존용: 옛 방(=스레드) id 로 조회하면 그 스레드 메시지만 돌려준다.
    // 이관 후 메시지의 chat_room_id 는 컨테이너지만 thread_id 는 옛 방 id 로 남는다.
    @Query("""
           SELECT c FROM chat c
           WHERE c.threadId = :threadId AND c.id < :cursor
           ORDER BY c.id DESC
           """)
    List<ChatEntity> findByThreadIdAndCursor(Long threadId, Long cursor, Pageable pageable);

    @Query("""
           SELECT COUNT(c) FROM chat c
           WHERE c.threadId = :threadId
           """)
    int countByThreadId(Long threadId);

    // 웹 스레드 탭: 컨테이너 안에서 특정 스레드만 필터.
    @Query("""
           SELECT c FROM chat c
           WHERE c.chatRoomId.id = :roomId AND c.threadId = :threadId AND c.id < :cursor
           ORDER BY c.id DESC
           """)
    List<ChatEntity> findByRoomIdAndThreadIdAndCursor(Long roomId, Long threadId, Long cursor, Pageable pageable);

    @Query("""
           SELECT COUNT(c) FROM chat c
           WHERE c.chatRoomId.id = :roomId AND c.threadId = :threadId
           """)
    int countByRoomIdAndThreadId(Long roomId, Long threadId);


    // 가장 최근대화 불러오기.
    // v1.3.5: 호출부는 주제(스레드) id 를 넘긴다. 이관 전/후 모두 주제 메시지의 최신 시각을 반환.
    @Query("""
            SELECT c.timeStamp FROM chat c
            WHERE (c.threadId = :chatRoomId OR (c.threadId IS NULL AND c.chatRoomId.id = :chatRoomId))
            ORDER BY c.timeStamp
            DESC
            LIMIT 1
            """)
    Optional<LocalDateTime> findMostRecentMessageTimestampByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
