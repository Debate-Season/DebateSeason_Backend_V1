package com.debateseason_backend_v1.domain.chatroom.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ChatRoomControllerV1 {

	private final ChatRoomServiceV1 chatRoomServiceV1;

	@Operation(
		summary = "채팅방(=안건=토론방)생성하기",
		description = "title,content -> JSON, issue_id = 쿼리스트링")
	// 4. 채팅방(=안건=토론방)생성하기, title,content -> JSON, issue_id = 쿼리스트링
	@PostMapping("/room")
	public ApiResult<Object> createChatRoom(
		@RequestBody ChatRoomDTO chatRoomDTO,
		@RequestParam(name = "issue-id") Long issue_id) {
		return chatRoomServiceV1.save(chatRoomDTO, issue_id);
	}

	// 4. 채팅방 단건 불러오기
	@Operation(
		summary = "채팅방 단건 불러오기",
		description = "채팅방 상세보기")
	@GetMapping("/room")
	public ApiResult<Object> getChatRoom(
		@RequestParam(name = "chatroom-id") Long chatRoomId,
		@AuthenticationPrincipal CustomUserDetails principal) {

		Long userId = principal.getUserId();
		return chatRoomServiceV1.fetch(userId,chatRoomId);
	}

	// 5. 채팅방 찬성/반대 투표하기, opinion, chatroomid = 쿼리스트링
	@Operation(
		summary = "채팅방 찬성/반대 투표하기",
		description = "opinion, chatroomid = 쿼리스트링")
	@PostMapping("/room/vote")
	public ApiResult<Object> voteChatRoom(
		@RequestParam(name = "opinion") String opinion,
		@RequestParam(name = "chatroom-id") Long chatRoomId,
		@AuthenticationPrincipal CustomUserDetails principal) {

		Long userId = principal.getUserId();
		return chatRoomServiceV1.vote(opinion, chatRoomId, userId);
	}

	// 5. 내가 투표한 토론방 가져오기
	@GetMapping("/room/voted")
	public ApiResult<Object> getVotedChatRoom(
		@AuthenticationPrincipal CustomUserDetails principal,
		@RequestParam(name = "page") int page
		){
		Long userId = principal.getUserId();
		return chatRoomServiceV1.findVotedChatRoom(userId,page);

	}
	

}


