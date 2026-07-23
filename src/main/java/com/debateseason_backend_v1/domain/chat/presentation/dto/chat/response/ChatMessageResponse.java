package com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatReactionRepository;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatReactionRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {


    @Schema(description = "채팅 ID",example = "1L")
    private Long id;
    @Schema(description = "룸ID(컨테이너)",example = "1L")
    private Long roomId;
    @Schema(description = "스레드ID. 웹이 탭 필터에 사용. 미분류 메시지는 null", example = "1L")
    private Long threadId;
    @Schema(description = "메시지 타입", example = "CHAT")
    private MessageType messageType;
    @NotBlank(message= "메시지 내용은 필수 입니다.")
    @Schema(description = "메시지 내용 (메시지는 1자 이상 500자 이하여야 합니다.)" , example = "안녕하세요.")
    private String content;
    @Schema(description = "발신자", example = "홍길동")
    private String sender;
    @Schema(description = "토론찬반", example = "AGREE")
    private OpinionType opinionType;
    @Schema(description = "사용자 소속 커뮤니티", example = "에펨코리아")
    private String userCommunity;
    @Schema(description = "프로필 색상", example = "RED")
    private String profileColor;

    @JsonSerialize(using = LocalDateTimeSerializer.class) // 직렬화 시 필요
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 역직렬화 시 필요
    @Schema(description = "메시지 받은 날짜 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime timeStamp;

    @Schema(description = "이모티콘 반응 정보")
    private ChatReactionResponse reactions;

    public static ChatMessageResponse from(ChatMessageRequest request) {
        return ChatMessageResponse.builder()
                .threadId(request.getThreadId())
                .messageType(request.getMessageType())
                .sender(request.getSender())
                .content(request.getContent())
                .opinionType(request.getOpinionType())
                .userCommunity(request.getUserCommunity())
                .timeStamp(request.getTimeStamp())
                .build();
    }

    public static ChatMessageResponse from(ChatEntity chat, Long currentUserId, ChatReactionRepository chatReactionRepository) {
        return from(chat, currentUserId, chatReactionRepository, null);
    }

    public static ChatMessageResponse from(ChatEntity chat, Long currentUserId, ChatReactionRepository chatReactionRepository, String profileColor) {
        // 반응 수 조회
        int logicCount = chatReactionRepository.countByChatIdAndReactionType(
                chat.getId(), ChatReactionRequest.ReactionType.LOGIC);
        int attitudeCount = chatReactionRepository.countByChatIdAndReactionType(
                chat.getId(), ChatReactionRequest.ReactionType.ATTITUDE);

        // 현재 사용자의 반응 여부 조회
        boolean userReactedLogic = false;
        boolean userReactedAttitude = false;

        if (currentUserId != null) {
            userReactedLogic = chatReactionRepository.findByChatIdAndUserIdAndReactionType(
                    chat.getId(), currentUserId, ChatReactionRequest.ReactionType.LOGIC).isPresent();
            userReactedAttitude = chatReactionRepository.findByChatIdAndUserIdAndReactionType(
                    chat.getId(), currentUserId, ChatReactionRequest.ReactionType.ATTITUDE).isPresent();
        }

        return ChatMessageResponse.builder()
                .id(chat.getId())
                .roomId(chat.getChatRoomId().getId())
                .threadId(chat.getThreadId())
                .messageType(chat.getMessageType())
                .content(chat.getContent())
                .sender(chat.getSender())
                .opinionType(chat.getOpinionType())
                .userCommunity(chat.getUserCommunity())
                .profileColor(profileColor)
                .timeStamp(chat.getTimeStamp())
                .reactions(ChatReactionResponse.builder()
                        .logicCount(logicCount)
                        .attitudeCount(attitudeCount)
                        .userReactedLogic(userReactedLogic)
                        .userReactedAttitude(userReactedAttitude)
                        .build())
                .build();
    }

    public static ChatMessageResponse from(ChatEntity chat) {
        return from(chat, (String) null);
    }

    /**
     * 실시간 브로드캐스트용. 저장은 컨테이너(chat_room_id)로 하되, 응답의 roomId 는 클라이언트가
     * 실제로 구독/발신한 주소(addressedRoomId)로 맞춘다.
     * - 구 앱: 옛 방(스레드) id 로 발신·구독 → roomId 를 그 방 id 로 유지해 호환.
     * - 신 클라이언트: 컨테이너로 발신 → addressedRoomId == 컨테이너 == chat_room_id (동일).
     * threadId 는 스레드 태그로 payload 에 함께 실려 웹 탭 필터에 쓰인다.
     */
    public static ChatMessageResponse from(ChatEntity chat, String profileColor, Long addressedRoomId) {
        ChatMessageResponse base = from(chat, profileColor);
        base.roomId = addressedRoomId != null ? addressedRoomId : base.roomId;
        return base;
    }

    public static ChatMessageResponse from(ChatEntity chat, String profileColor) {
        // 빈 ReactionResponse 객체 생성
        ChatReactionResponse emptyReaction = ChatReactionResponse.builder()
                .logicCount(0)
                .attitudeCount(0)
                .userReactedLogic(false)
                .userReactedAttitude(false)
                .build();

        return ChatMessageResponse.builder()
                .id(chat.getId())
                .roomId(chat.getChatRoomId().getId())
                .threadId(chat.getThreadId())
                .messageType(chat.getMessageType())
                .content(chat.getContent())
                .sender(chat.getSender())
                .opinionType(chat.getOpinionType())
                .userCommunity(chat.getUserCommunity())
                .profileColor(profileColor)
                .timeStamp(chat.getTimeStamp())
                .reactions(emptyReaction)
                .build();
    }

    /**
     * 최적화된 팩토리 메서드
     *
     * 기존 from() vs 새로운 fromOptimized() 비교
     * - 기존: Repository 직접 호출 → 매번 DB 쿼리
     * - 개선: Map에서 조회 → 메모리에서 O(1) 조회
     *
     * @param chat 채팅 엔티티
     * @param currentUserId 현재 사용자 ID
     * @param reactionCountsMap 미리 조회한 반응 수 Map
     * @param userReactionsMap 미리 조회한 사용자 반응 Map
     */
    public static ChatMessageResponse fromOptimized(
            ChatEntity chat,
            Long currentUserId,
            Map<Long, Map<ChatReactionRequest.ReactionType, Integer>> reactionCountsMap,
            Map<Long, Set<ChatReactionRequest.ReactionType>> userReactionsMap,
            Map<Long, String> profileColorMap) {

        Map<ChatReactionRequest.ReactionType, Integer> reactionCounts =
                reactionCountsMap.getOrDefault(chat.getId(), Collections.emptyMap());

        int logicCount = reactionCounts.getOrDefault(ChatReactionRequest.ReactionType.LOGIC, 0);
        int attitudeCount = reactionCounts.getOrDefault(ChatReactionRequest.ReactionType.ATTITUDE, 0);

        Set<ChatReactionRequest.ReactionType> userReactions =
                userReactionsMap.getOrDefault(chat.getId(), Collections.emptySet());

        boolean userReactedLogic = userReactions.contains(ChatReactionRequest.ReactionType.LOGIC);
        boolean userReactedAttitude = userReactions.contains(ChatReactionRequest.ReactionType.ATTITUDE);

        String profileColor = chat.getUserId() != null
                ? profileColorMap.getOrDefault(chat.getUserId(), null)
                : null;

        return ChatMessageResponse.builder()
                .id(chat.getId())
                .roomId(chat.getChatRoomId().getId())
                .threadId(chat.getThreadId())
                .messageType(chat.getMessageType())
                .content(chat.getContent())
                .sender(chat.getSender())
                .opinionType(chat.getOpinionType())
                .userCommunity(chat.getUserCommunity())
                .profileColor(profileColor)
                .timeStamp(chat.getTimeStamp())
                .reactions(ChatReactionResponse.builder()
                        .logicCount(logicCount)
                        .attitudeCount(attitudeCount)
                        .userReactedLogic(userReactedLogic)
                        .userReactedAttitude(userReactedAttitude)
                        .build())
                .build();
    }
}


