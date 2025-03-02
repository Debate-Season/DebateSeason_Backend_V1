package com.debateseason_backend_v1.domain.profile.controller.docs;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.profile.service.response.CommunityResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Community API", description = "커뮤니티 관련 API")
public interface CommunityControllerV1Docs {

	@Operation(
		summary = "커뮤니티 목록 조회",
		description = "전체 커뮤니티 목록을 조회합니다"
	)
	@ApiResponse(responseCode = "200", description = "커뮤니티 목록 조회 성공")
	public ApiResult<List<CommunityResponse>> getCommunities(
	);

	@Operation(
		summary = "커뮤니티 검색",
		description = """
			커뮤니티를 검색해 조회합니다. 검색어가 없을 경우 전체 커뮤니티 목록을 반환합니다.
			검색어를 포함하는 커뮤니티가 없을 경우 빈 목록을 반환합니다.
			"""
	)
	@Parameter(
		name = "query",
		description = "검색할 커뮤니티 이름",
		required = true,
		schema = @Schema(type = "string"),
		example = "디시"
	)
	@ApiResponse(responseCode = "200", description = "커뮤니티 검색 성공")
	public ApiResult<List<CommunityResponse>> searchCommunities(
		@RequestParam String query
	);

}