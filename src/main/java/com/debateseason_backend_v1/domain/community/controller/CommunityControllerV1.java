package com.debateseason_backend_v1.domain.community.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.PageMetaResponse;
import com.debateseason_backend_v1.domain.community.service.CommunityServiceV1;
import com.debateseason_backend_v1.domain.community.service.response.CommunityResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/communities")
public class CommunityControllerV1 {

	private final CommunityServiceV1 communityService;

	@GetMapping
	public ApiResult<List<CommunityResponse>> getCommunities(
		@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC) Pageable pageable
	) {

		Page<CommunityResponse> communities = communityService.getCommunities(pageable);

		return ApiResult.success(
			"커뮤니티 목록 조회가 완료되었습니다.",
			communities.getContent(),
			PageMetaResponse.of(communities)
		);
	}

	@GetMapping("/search")
	public ApiResult<List<CommunityResponse>> searchCommunities(
		@RequestParam String name,
		@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
	) {

		Page<CommunityResponse> search = communityService.searchByName(name, pageable);

		return ApiResult.success(
			"커뮤니티 검색이 완료되었습니다.",
			search.getContent(),
			PageMetaResponse.of(search)
		);
	}

}
