package com.debateseason_backend_v1.domain.chat.infrastructure.chat_reaction;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatReactionRepository;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatReactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ChatReactionRepositoryImpl implements ChatReactionRepository {

    private final ChatReactionJpaRepository chatReactionJpaRepository;


    @Override
    public void save(ChatReaction chatReaction) {
     chatReactionJpaRepository.save(chatReaction);
    }
    @Override
    public Optional<ChatReaction> findByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType) {
        return chatReactionJpaRepository.findByChatIdAndUserIdAndReactionType(chatId, userId, reactionType);
    }

    @Override
    public int countByChatIdAndReactionType(Long chatId, ChatReactionRequest.ReactionType reactionType) {
        return chatReactionJpaRepository.countByChatIdAndReactionType(chatId, reactionType);
    }

    @Override
    public List<ChatReaction> findByChatId(Long chatId) {
        return chatReactionJpaRepository.findByChatId(chatId);
    }

    @Override
    public List<ChatReaction> findByChatIdAndUserId(Long chatId, Long userId) {
        return chatReactionJpaRepository.findByChatIdAndUserId(chatId, userId);
    }

    @Override
    public List<Long> findUserIdsByChatIdAndReactionType(Long chatId, ChatReactionRequest.ReactionType reactionType) {
        return chatReactionJpaRepository.findUserIdsByChatIdAndReactionType(chatId, reactionType);
    }

    @Override
    public void deleteByChatIdAndUserIdAndReactionType(Long chatId, Long userId, ChatReactionRequest.ReactionType reactionType) {
        chatReactionJpaRepository.deleteByChatIdAndUserIdAndReactionType(chatId, userId, reactionType);
    }
}
