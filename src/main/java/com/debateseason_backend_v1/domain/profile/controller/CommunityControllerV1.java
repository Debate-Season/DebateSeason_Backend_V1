package com.debateseason_backend_v1.domain.profile.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.profile.controller.docs.CommunityControllerV1Docs;
import com.debateseason_backend_v1.domain.profile.service.CommunityServiceV1;
import com.debateseason_backend_v1.domain.profile.service.response.CommunityResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/communities")
public class CommunityControllerV1 implements CommunityControllerV1Docs {

	private final CommunityServiceV1 communityService;

	@GetMapping
	public ApiResult<List<CommunityResponse>> getCommunities(
	) {

		List<CommunityResponse> communities = communityService.getCommunities();

		return ApiResult.success(
			"커뮤니티 목록 조회가 완료되었습니다.",
			communities
		);
	}

	@GetMapping("/search")
	public ApiResult<List<CommunityResponse>> searchCommunities(
		@RequestParam String query
	) {

		List<CommunityResponse> search = communityService.searchByName(query);

		return ApiResult.success(
			"커뮤니티 검색이 완료되었습니다.",
			search
		);
	}

}
