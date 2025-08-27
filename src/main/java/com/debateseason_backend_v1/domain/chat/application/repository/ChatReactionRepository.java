package com.debateseason_backend_v1.domain.chat.application.repository;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat_reaction.ChatReaction;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatReactionRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ChatReactionRepository {

    void save(ChatReaction chatReaction);

    Optional<ChatReaction> findByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType);

    int countByChatIdAndReactionType(Long chatId, ChatReactionRequest.ReactionType reactionType);

    List<ChatReaction> findByChatId(Long chatId);

    List<ChatReaction> findByChatIdAndUserId(Long chatId, Long userId);

    List<Long> findUserIdsByChatIdAndReactionType(Long chatId, ChatReactionRequest.ReactionType reactionType);

    void deleteByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType);

    /**
     * 여러 채팅의 반응 수를 한 번에 조회
     *
     * @param chatIds 조회할 채팅 ID 목록
     * @return Map<채팅ID, Map<반응타입, 반응수>>
     *
     */
    Map<Long, Map<ChatReactionRequest.ReactionType, Integer>> findReactionCountsByChatIdsIn(List<Long> chatIds);


    /**
     * 특정 사용자가 여러 채팅에 남긴 반응을 한 번에 조회
     *
     * @param chatIds 조회할 채팅 ID 목록
     * @param userId 사용자 ID
     * @return Map<채팅ID, Set<반응타입>>
     *
     */
    Map<Long, Set<ChatReactionRequest.ReactionType>> findUserReactionsByChatIdsIn(List<Long> chatIds, Long userId);

}
