package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.chat.model.request.ChatReactionRequest;
import com.debateseason_backend_v1.domain.repository.entity.ChatReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatReactionRepository extends JpaRepository<ChatReaction, Long> {

    // 특정 채팅에 사용자의 특정 타입 반응 조회
    Optional<ChatReaction> findByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType);

    // 특정 채팅 메시지에 대한 특정 타입의 반응 수 조회
    @Query("SELECT COUNT(cr) FROM ChatReaction cr WHERE cr.chat.id = :chatId AND cr.reactionType = :reactionType")
    int countByChatIdAndReactionType(@Param("chatId") Long chatId, @Param("reactionType") ChatReactionRequest.ReactionType reactionType);

    // 특정 채팅 메시지에 대한 모든 반응 조회
    List<ChatReaction> findByChatId(Long chatId);

    // 사용자의 모든 반응 조회
    List<ChatReaction> findByChatIdAndUserId(Long chatId, Long userId);

    // 특정 타입의 반응을 한 사용자 ID 목록 조회
    @Query("SELECT cr.userId FROM ChatReaction cr WHERE cr.chat.id = :chatId AND cr.reactionType = :reactionType")
    List<Long> findUserIdsByChatIdAndReactionType(@Param("chatId") Long chatId, @Param("reactionType") ChatReactionRequest.ReactionType reactionType);

    // 사용자의  반응 삭제
    void deleteByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType);

}
