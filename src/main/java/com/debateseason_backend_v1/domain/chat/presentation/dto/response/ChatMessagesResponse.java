package com.debateseason_backend_v1.domain.chat.presentation.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "채팅 메시지 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagesResponse {
    
    @Schema(description = "채팅 메시지 목록")
    private List<ChatMessageResponse> items;
    
    @Schema(description = "추가 메시지 존재 여부", example = "true")
    private boolean hasMore;
    
    @Schema(description = "다음 페이지 조회를 위한 커서", example = "123")
    private String nextCursor;
    
    @Schema(description = "전체 메시지 수", example = "50")
    private int totalCount;
}
