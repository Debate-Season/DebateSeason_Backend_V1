package com.debateseason_backend_v1.domain.chatroom.docs;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.issue.docs.CustomApiErrorCode;
import com.debateseason_backend_v1.domain.issue.docs.CustomErrorCode;
import com.debateseason_backend_v1.domain.issue.dto.IssueDAO;
import com.debateseason_backend_v1.domain.issue.model.response.IssueResponse;
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
	@CustomApiErrorCode(
		{
			CustomErrorCode.NOT_FOUND_ISSUE
		}
	)
	@Parameter(
		name = "issue-id",
		description = "이슈방 id",
		required = true,
		example = "1"
	)
	public ApiResult<Object> createChatRoom(
		@RequestBody ChatRoomDTO chatRoomDTO,
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
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "해당 채팅방을 성공적으로 불러왔습니다."),
		@ApiResponse(responseCode = "400", description = "해당 채팅방을 불러오지 못했습니다.")
	})
	@CustomApiErrorCode(
		{
			CustomErrorCode.NOT_FOUND_ISSUE
		}
	)
	public ApiResult<ChatRoomDAO> getChatRoom(
		@RequestParam(name = "chatroom-id") Long chatRoomId,
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
	@CustomApiErrorCode(
		{
			CustomErrorCode.NOT_FOUND_ISSUE
		}
	)
	public ApiResult<String> voteChatRoom(
		@RequestParam(name = "opinion") String opinion,
		@RequestParam(name = "chatroom-id") Long chatRoomId,
		@AuthenticationPrincipal CustomUserDetails principal)
		;
}
