package com.debateseason_backend_v1.domain.chatroom.docs;



import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.model.response.etc.Opinion;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ChatRoomResponse;
import com.debateseason_backend_v1.domain.chatroom.model.request.ChatRoomRequest;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ChatRoom API", description = "채팅방 관련된 모든 API")
public interface ChatRoomControllerV1Docs {
	@Operation(
		summary = "채팅방 1건을 생성",
		description = "ADMIN이 채팅방 1건을 생성을 생성합니다"
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "성공적으로 채팅방을 생성했습니다."),

	})
	@Parameter(
		name = "issue-id",
		description = "이슈방 id",
		required = true,
		example = "1"
	)
	public ApiResult<Object> createChatRoom(
		@RequestBody ChatRoomRequest chatRoomRequest,
		@RequestParam(name = "issue-id") Long issue_id
	);


	//
	@Operation(
		summary = "채팅방 1건 불러오기",
		description = "채팅방 1건을 불러옵니다.",
		security = @SecurityRequirement(name = "JWT")
	)
	@Parameter(
		name = "chatroom-id",
		description = "채팅방 id",
		required = true,
		example = "1",
		schema = @Schema(type = "string")
	)
	/*
	@Parameter(
		name = "type",
		description = "토론위키(wiki)냐 하이라이트(highlight)냐 없어도 상관없음",
		required = false,
		example = "highlight",
		schema = @Schema(type = "string")
	)

	 */
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "해당 채팅방을 성공적으로 불러왔습니다."),
		@ApiResponse(responseCode = "400", description = "해당 채팅방을 불러오지 못했습니다.")
	})
	public ApiResult<ChatRoomResponse> getChatRoom(
		@RequestParam(name = "chatroom-id") Long chatRoomId,
		//@RequestParam(name = "type",required = false) String type,
		@AuthenticationPrincipal CustomUserDetails principal);
	//

	@Operation(
		summary = "채팅방 투표하기",
		description = "해당 채팅방에 대해서 AGREE, DISAGREE 투표를 합니다.",
		security = @SecurityRequirement(name = "JWT")
	)
	@Parameter(
		name = "opinion",
		description = "AGREE 또는 DISAGREE 투표를 합니다.",
		required = true,
		example = "1",
		schema = @Schema(type = "string")
	)
	@Parameter(
		name = "chatroom-id",
		description = "채팅방 ID",
		required = true,
		example = "1",
		schema = @Schema(type = "string")
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "성공적으로 투표했습니다."),

	})
	public ApiResult<String> voteChatRoom(
		@RequestParam(name = "opinion") Opinion opinion,
		@RequestParam(name = "chatroom-id") Long chatRoomId,
		@AuthenticationPrincipal CustomUserDetails principal)
		;
}
