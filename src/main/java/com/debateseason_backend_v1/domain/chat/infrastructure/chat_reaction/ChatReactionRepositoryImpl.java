package com.debateseason_backend_v1.domain.chat.infrastructure.chat_reaction;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatReactionRepository;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.ReactionCountDto;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatReactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

    @Override
    public Map<Long, Map<ChatReactionRequest.ReactionType, Integer>> findReactionCountsByChatIdsIn(List<Long> chatIds){
        if (chatIds == null || chatIds.isEmpty()) {
            log.error("@@ chatIds가 null or empty 입니다.");
            log.error("@@ chatIds = {}", chatIds);
            return Collections.emptyMap();
        }

        List<ReactionCountDto> counts = chatReactionJpaRepository.findReactionCountsByChatIdsIn(chatIds);

        return counts.stream()
                .collect(Collectors.groupingBy(
                        ReactionCountDto::getChatId,                    // 외부 Map의 키: chatId
                        Collectors.toMap(
                                ReactionCountDto::getReactionType,          // 내부 Map의 키: reactionType
                                dto -> dto.getCount().intValue(),           // 내부 Map의 값: count
                                (existing, replacement) -> existing         // 중복 키 처리 (발생하지 않아야 함)
                        )
                ));

    }

    @Override
    public Map<Long, Set<ChatReactionRequest.ReactionType>> findUserReactionsByChatIdsIn(List<Long> chatIds, Long userId){
        if (chatIds == null || chatIds.isEmpty() || userId == null) {
            return Collections.emptyMap();
        }

        List<ChatReaction> userReactions = chatReactionJpaRepository.findUserReactionsByChatIdsIn(chatIds, userId);

        return userReactions.stream()
                .collect(Collectors.groupingBy(
                        chatReaction -> chatReaction.getChat().getId(),
                        Collectors.mapping(
                                ChatReaction::getReactionType,
                                Collectors.toSet()
                        )
                ));
    }

}
