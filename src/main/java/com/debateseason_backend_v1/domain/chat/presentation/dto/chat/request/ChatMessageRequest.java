package com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {

    @Schema(description = "룸ID",example = "1L")
    private Long roomId;
    @Schema(description = "메시지 타입", example = "CHAT")
    private MessageType messageType;
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

    public static ChatMessageRequest of(Long roomId, String sender, String content,
                                        OpinionType opinionType, String userCommunity) {
        return ChatMessageRequest.builder()
                .roomId(roomId)
                .messageType(MessageType.CHAT)
                .sender(sender)
                .content(content)
                .opinionType(opinionType)
                .userCommunity(userCommunity)
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
