package com.debateseason_backend_v1.media.docs;

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.media.model.MediaType;
import com.debateseason_backend_v1.media.model.response.MediaContainer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ChatRoom API", description = "채팅방 관련된 모든 API")
public interface MediaControllerV1Docs {



	//
	@Operation(
		summary = "미디어 불러오기",
		description = "미디어(youtube, news, commnunity)를 불러옵니다"
	)
	@Parameter(
		name = "type",
		description = "미디어 type(youtube, news, community). 만약 아무것도 입력하지 않은 경우 -> 전체",
		required = false,
		example = "youtube",
		schema = @Schema(type = "string")
	)
	@Parameter(
		name = "time",
		description = "날짜 기반으로 커서 페이지네이션 진행",
		required = false,
		example = "youtube",
		schema = @Schema(type = "string")
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "해당 채팅방을 성공적으로 불러왔습니다."),
		@ApiResponse(responseCode = "400", description = "해당 채팅방을 불러오지 못했습니다.")
	})
	public ApiResult<MediaContainer> getMedia(
		@RequestParam(name = "type",required = false) @Nullable MediaType mediaType,
		@RequestParam(name = "time",required = false)String time)
;


}
