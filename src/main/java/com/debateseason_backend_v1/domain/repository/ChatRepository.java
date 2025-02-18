package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.repository.entity.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat,Long> {

    @Query("""
           SELECT c FROM Chat c 
           WHERE c.chatRoomId.id = :roomId 
           AND c.id < :cursor 
           AND DATE(c.timeStamp) = :date 
           ORDER BY c.id DESC
           """)
    List<Chat> findByRoomIdAndCursorAndDate(
            @Param("roomId") Long roomId,
            @Param("cursor") Long cursor,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    @Query("""
           SELECT COUNT(c) FROM Chat c 
           WHERE c.chatRoomId.id = :roomId 
           AND DATE(c.timeStamp) = :date
           """)
    long countByRoomIdAndDate(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date
    );
}
