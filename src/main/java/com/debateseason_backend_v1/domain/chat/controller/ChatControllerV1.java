package com.debateseason_backend_v1.domain.chat.controller;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessagesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;
import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Slf4j
@Tag(name = "Chat API", description = "V1 Chat API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatControllerV1 {

	private final ChatServiceV1 chatService;


	/*
	날짜당 최대 20개 메시지 조회
	다음 페이지를 위한 커서 제공
	날짜별 그루핑
	각 날짜별 메시지 수와 추가 데이터 존재 여부 표시
	최대 7일치 데이터 조회
	클라이언트는 nextCursor 를 이용하여 추가데이터를 요청 할 수 있고, hasMore 로 추가데이터 존재여부, totalCount로 전체메시지 수 확인 가능
	*/
	@Operation(
		summary = "채팅방 메시지 조회",
		description = "특정 채팅방의 메시지를 커서 기반으로 조회합니다. " +
				"날짜별로 그룹핑되어 최근 7일치 데이터를 제공하며, " +
				"각 날짜당 최대 20개의 메시지를 조회합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ChatMessagesResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청"
		),
		@ApiResponse(
			responseCode = "404",
			description = "채팅방을 찾을 수 없음"
		)
	})
	@GetMapping("/rooms/{roomId}/messages")
	public ApiResult<ChatMessagesResponse> getChatMessages(
		@Parameter(
			description = "채팅방 ID",
			required = true,
			example = "1"
		)
		@PathVariable Long roomId,
		
		@Parameter(
			description = "다음 페이지 조회를 위한 커서 (마지막 메시지 ID). " +
						"첫 페이지 조회 시에는 미입력",
			required = false,
			example = "1234"
		)
		@RequestParam(required = false) Long cursor
	) {
		return chatService.getChatMessages(roomId, cursor);
	}

}
