package com.debateseason_backend_v1.domain.community.controller.docs;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.community.service.response.CommunityResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Community API", description = "커뮤니티 관련 API")
public interface CommunityControllerV1Docs {

	@Operation(
		summary = "커뮤니티 목록 조회",
		description = "전체 커뮤니티 목록을 이름순으로 오름차순 정렬하여 조회합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "커뮤니티 목록 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResult.class),
				examples = @ExampleObject(
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "커뮤니티 목록 조회가 완료되었습니다.",
						    "data": [
						        {
						            "id": 20,
						            "name": "Reddit",
						            "iconUrl": "community/icons/reddit.png"
						        },
						        {
						            "id": 22,
						            "name": "Threads",
						            "iconUrl": "community/icons/threads.png"
						        }
						    ]
						}
						"""
				)
			)
		)
	})
	public ApiResult<List<CommunityResponse>> getCommunities();

	@Operation(
		summary = "커뮤니티 검색",
		description = "커뮤니티 이름으로 검색하여 이름순으로 정렬된 결과를 반환합니다."
	)
	@Parameter(
		name = "query",
		description = "검색할 커뮤니티 이름",
		required = true,
		schema = @Schema(type = "string"),
		example = "디시"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "커뮤니티 검색 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResult.class),
				examples = {
					@ExampleObject(
						name = "SearchSuccess",
						summary = "검색 결과가 있는 경우",
						value = """
							{
							    "status": 200,
							    "code": "SUCCESS",
							    "message": "커뮤니티 검색이 완료되었습니다.",
							    "data": [
							        {
							            "id": 1,
							            "name": "디시인사이드",
							            "iconUrl": "community/icons/dcinside.png"
							        }
							    ]
							}
							"""
					),
					@ExampleObject(
						name = "SearchEmpty",
						summary = "검색 결과가 없는 경우",
						value = """
							{
							    "status": 200,
							    "code": "SUCCESS",
							    "message": "커뮤니티 검색이 완료되었습니다.",
							    "data": []
							}
							"""
					)
				}
			)
		)
	})
	public ApiResult<List<CommunityResponse>> searchCommunities(@RequestParam String query);
}