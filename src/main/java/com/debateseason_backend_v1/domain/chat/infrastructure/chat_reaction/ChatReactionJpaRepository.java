package com.debateseason_backend_v1.domain.chat.infrastructure.chat_reaction;

import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.ReactionCountDto;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatReactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatReactionJpaRepository extends JpaRepository<ChatReaction, Long> {

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

    /**
     *  chatId 로 메시지 반응 조회
     */
    @Query("""
        select new com.debateseason_backend_v1.domain.chat.presentation.dto.chat.ReactionCountDto(
            cr.chat.id,
            cr.reactionType,
            count(cr)
            )
        from ChatReaction cr
        where cr.chat.id IN :chatIds
        group by cr.chat.id, cr.reactionType
    """)
    List<ReactionCountDto> findReactionCountsByChatIdsIn(@Param("chatIds") List<Long> chatIds);

    /**
     *  사용자의 반응
     */
    @Query("""
        select cr
        from ChatReaction cr
        where cr.chat.id in :chatIds
        and cr.userId = :userId
    """)
    List<ChatReaction> findUserReactionsByChatIdsIn(@Param("chatIds") List<Long> chatIds, @Param("userId") Long userId);

}
