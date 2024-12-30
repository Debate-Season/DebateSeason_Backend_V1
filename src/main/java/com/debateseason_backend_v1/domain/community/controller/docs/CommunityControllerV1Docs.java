package com.debateseason_backend_v1.domain.community.controller.docs;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.community.service.response.CommunityResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
		description = "페이징 처리된 전체 커뮤니티 목록을 조회합니다."
	)
	@Parameter(
		name = "pageable",
		description = """
			페이지 정보:
			- page: 페이지 번호 (0부터 시작)
			- size: 페이지 크기 (기본값: 20)
			- sort: 정렬 기준 (기본값: name,desc)
			""",
		schema = @Schema(type = "string"),
		example = "page=0&size=20"
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
						            "id": 1,
						            "name": "디시인사이드",
						            "iconUrl": "community/icons/dcinside.png"
						        },
						        {
						            "id": 2,
						            "name": "에펨코리아",
						            "iconUrl": "community/icons/fmkorea.png"
						        }
						    ],
						    "meta": {
						        "page": 0,
						        "size": 20,
						        "totalElements": 50,
						        "totalPages": 3
						    }
						}
						"""
				)
			)
		)
	})
	public ApiResult<List<CommunityResponse>> getCommunities(
		@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC) Pageable pageable
	);

	@Operation(
		summary = "커뮤니티 검색",
		description = "커뮤니티 이름으로 검색합니다."
	)
	@Parameters({
		@Parameter(
			name = "name",
			description = "검색할 커뮤니티 이름",
			required = true,
			schema = @Schema(type = "string"),
			example = "운동"
		),
		@Parameter(
			name = "pageable",
			description = """
				페이지 정보:
				- page: 페이지 번호 (0부터 시작)
				- size: 페이지 크기 (기본값: 20)
				- sort: 정렬 기준 (기본값: name,desc)
				""",
			schema = @Schema(type = "string"),
			example = "page=0&size=20"
		)
	})
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "커뮤니티 검색 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResult.class),
				examples = @ExampleObject(
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
						        },
						        {
						            "id": 5,
						            "name": "에펨코리아",
						            "iconUrl": "community/icons/fmkorea.png"
						        }
						    ],
						    "meta": {
						        "page": 0,
						        "size": 20,
						        "totalElements": 2,
						        "totalPages": 1
						    }
						}
						"""
				)
			)
		)
	})
	public ApiResult<List<CommunityResponse>> searchCommunities(
		@RequestParam String name,
		@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC) Pageable pageable
	);
}