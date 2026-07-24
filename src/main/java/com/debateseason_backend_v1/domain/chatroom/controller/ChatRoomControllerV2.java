package com.debateseason_backend_v1.domain.chatroom.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.RoomContainerResponse;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV2;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "ChatRoom API V2", description = "v1.3.5 스레드 통합 — 컨테이너 진입점")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2")
public class ChatRoomControllerV2 {

	private final ChatRoomServiceV2 chatRoomServiceV2;

	// 신 클라이언트가 이슈로 진입할 때: 컨테이너 방 1개 + 스레드 목록.
	// optional-auth: 로그인 시 스레드별 내 투표(myOpinion)를 채우고, 비로그인도 조회 가능.
	@Operation(
		summary = "이슈의 컨테이너 채팅방 + 스레드 목록 조회",
		description = "이슈당 컨테이너 방 1개와 그 하위 스레드(=기존 방) 목록을 반환합니다. 로그인 시 스레드별 내 투표가 채워집니다."
	)
	@Parameter(name = "issue-id", description = "이슈 id", required = true, example = "1")
	@GetMapping("/room")
	public ApiResult<RoomContainerResponse> fetchByIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal) {

		Long userId = principal != null ? principal.getUserId() : null;

		RoomContainerResponse response = chatRoomServiceV2.getRoomByIssue(issueId, userId);

		return ApiResult.success("채팅방을 불러왔습니다.", response);
	}
}
