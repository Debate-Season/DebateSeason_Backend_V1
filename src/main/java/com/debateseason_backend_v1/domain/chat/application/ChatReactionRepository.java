package com.debateseason_backend_v1.domain.chat.application;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat_reaction.ChatReaction;
import com.debateseason_backend_v1.domain.chat.model.request.ChatReactionRequest;

import java.util.List;
import java.util.Optional;

public interface ChatReactionRepository {

    void save(ChatReaction chatReaction);

    Optional<ChatReaction> findByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType);

    int countByChatIdAndReactionType(Long chatId, ChatReactionRequest.ReactionType reactionType);

    List<ChatReaction> findByChatId(Long chatId);

    List<ChatReaction> findByChatIdAndUserId(Long chatId, Long userId);

    List<Long> findUserIdsByChatIdAndReactionType(Long chatId, ChatReactionRequest.ReactionType reactionType);

    void deleteByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType);


}
