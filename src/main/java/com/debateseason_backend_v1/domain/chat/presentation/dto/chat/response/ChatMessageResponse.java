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

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {


    @Schema(description = "채팅 ID",example = "1L")
    private Long id;
    @Schema(description = "룸ID",example = "1L")
    private Long roomId;
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

    @JsonSerialize(using = LocalDateTimeSerializer.class) // 직렬화 시 필요
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 역직렬화 시 필요
    @Schema(description = "메시지 받은 날짜 시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    private LocalDateTime timeStamp;

    @Schema(description = "이모티콘 반응 정보")
    private ChatReactionResponse reactions;

    public static ChatMessageResponse from(ChatMessageRequest request) {
        return ChatMessageResponse.builder()
                .messageType(request.getMessageType())
                .sender(request.getSender())
                .content(request.getContent())
                .opinionType(request.getOpinionType())
                .userCommunity(request.getUserCommunity())
                .timeStamp(request.getTimeStamp())
                .build();
    }

    public static ChatMessageResponse from(ChatEntity chat, Long currentUserId, ChatReactionRepository chatReactionRepository) {
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
                .messageType(chat.getMessageType())
                .content(chat.getContent())
                .sender(chat.getSender())
                .opinionType(chat.getOpinionType())
                .userCommunity(chat.getUserCommunity())
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
                .messageType(chat.getMessageType())
                .content(chat.getContent())
                .sender(chat.getSender())
                .opinionType(chat.getOpinionType())
                .userCommunity(chat.getUserCommunity())
                .timeStamp(chat.getTimeStamp())
                .reactions(emptyReaction)
                .build();
    }
}


