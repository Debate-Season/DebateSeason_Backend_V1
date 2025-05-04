package com.debateseason_backend_v1.domain.chat.presentation.controller;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.debateseason_backend_v1.domain.chat.presentation.dto.request.ChatReactionRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.response.ChatMessagesResponse;
import com.debateseason_backend_v1.domain.chat.application.service.ChatReactionServiceV1;
import com.debateseason_backend_v1.security.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.debateseason_backend_v1.domain.chat.application.service.ChatServiceV1;
import com.debateseason_backend_v1.domain.chat.presentation.dto.response.ChatMessageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "Chat API", description = "V1 Chat API")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatControllerV1 {

	private final ChatServiceV1 chatService;
	private final ChatReactionServiceV1 chatReactionService;
	private final JwtUtil jwtUtil;


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
				"한 번에 최대 20개의 메시지를 조회하며, 다음 페이지를 위한 커서를 제공합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ChatMessagesResponse.class),
				examples = @ExampleObject(
					value = """
						{
						  "status": 200,
						  "code": "SUCCESS",
						  "message": "채팅 메시지를 성공적으로 조회했습니다.",
						  "data": {
							"items": [
							  {
								"id": 1,
								"messageType": "CHAT",
								"sender": "홍길동",
								"content": "안녕하세요",
								"opinionType": "AGREE",
								"userCommunity": "에펨코리아",
								"timeStamp": "2025-03-23 11:11:11.123456"
							  }
							],
							"hasMore": true,
							"nextCursor": "123",
							"totalCount": 50
						  }
						}
						"""
				)
			)
		),
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
		@RequestParam(required = false) Long cursor,
		
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long userId,
		HttpServletRequest httpRequest
	) {
				// userId가 null인 경우 직접 토큰에서 추출
				if (userId == null) {
					String token = getJwtFromRequest(httpRequest);
					if (token != null) {
						try {
							userId = jwtUtil.getUserId(token);
							log.info("토큰에서 직접 추출한 userId: {}", userId);
						} catch (Exception e) {
							log.error("토큰에서 userId 추출 실패", e);
							throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "인증 정보를 확인할 수 없습니다");
						}
					}
					
					if (userId == null) {
						throw new CustomException(ErrorCode.NOT_FOUND_USER, "userId 가 null 입니다.");
					}
				}
		return chatService.getChatMessages(roomId, cursor, userId);
	}

	@Operation(
		summary = "채팅 메시지 이모티콘 반응 추가/제거",
		description = "특정 채팅 메시지에 이모티콘 반응을 추가 또는 제거 합니다."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "이모티콘 반응 처리 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ChatMessageResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "메시지를 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class)
			)
		)
	})
	@PostMapping("/messages/{messageId}/reactions")
	public ApiResult<ChatMessageResponse> reactToMessage(
		@Parameter(description = "메시지 ID", required = true, example = "1")
		@PathVariable Long messageId,
		
		@Valid @RequestBody ChatReactionRequest request,
		
		@Parameter(hidden = true)
		@AuthenticationPrincipal Long userId,
		
		HttpServletRequest httpRequest
	) {
		log.info("반응 처리 요청: messageId={}, reactionType={}, action={}, userId={}", 
				 messageId, request.getReactionType(), request.getAction(), userId);
		
		// userId가 null인 경우 직접 토큰에서 추출
		if (userId == null) {
			String token = getJwtFromRequest(httpRequest);
			if (token != null) {
				try {
					userId = jwtUtil.getUserId(token);
					log.info("토큰에서 직접 추출한 userId: {}", userId);
				} catch (Exception e) {
					log.error("토큰에서 userId 추출 실패", e);
					throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "인증 정보를 확인할 수 없습니다");
				}
			}
			
			if (userId == null) {
				throw new CustomException(ErrorCode.NOT_FOUND_USER, "userId 가 null 입니다.");
			}
		}
		
		return chatReactionService.processReaction(messageId, request, userId);
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

}
