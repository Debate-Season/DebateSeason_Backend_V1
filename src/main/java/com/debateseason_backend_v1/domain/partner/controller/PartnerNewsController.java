package com.debateseason_backend_v1.domain.partner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.partner.model.PartnerNewsResponse;
import com.debateseason_backend_v1.domain.partner.service.PartnerNewsService;
import com.debateseason_backend_v1.security.partner.PartnerApiKeyFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** 고객사 뉴스 API. 인증은 X-Partner-Key (PartnerApiKeyFilter). */
@Tag(name = "뉴스", description = "토론철 뉴스 — 목록 검색·상세 조회")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/partner/v1/news")
public class PartnerNewsController {

	private final PartnerNewsService partnerNewsService;

	@Operation(summary = "뉴스 목록 검색", description = "keyword 가 있으면 그 키워드가 연관 키워드에 정확히 일치하는 뉴스를 최신순으로, "
		+ "없으면 인기 뉴스(최근 7일, 조회 수 × 최신성)를 돌려줍니다. sort=latest 면 키워드 없이 최신순. "
		+ "키워드에 맞는 뉴스가 없으면 빈 목록입니다. 본문은 상세 조회에서 받습니다.")
	@GetMapping
	public ApiResult<PartnerNewsResponse.Page> list(
		@Parameter(hidden = true) @RequestAttribute(PartnerApiKeyFilter.PARTNER_ATTRIBUTE) String partner,
		@Parameter(description = "키워드 (장소 등). 앞뒤 공백은 무시하고 정확히 일치하는 것만 찾습니다", example = "강남역")
		@RequestParam(required = false) String keyword,
		@Parameter(description = "keyword 가 없을 때 정렬: popular(기본) | latest") @RequestParam(required = false) String sort,
		@Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
		@Parameter(description = "페이지 크기 (기본 20, 최대 50)") @RequestParam(required = false) Integer size) {

		return ApiResult.success("뉴스 목록", partnerNewsService.list(partner, keyword, sort, cursor, size));
	}

	@Operation(summary = "뉴스 상세 조회", description = "본문을 포함한 뉴스 상세. 조회 수가 1 오릅니다. 없거나 내려간 뉴스는 404 NOT_FOUND_NEWS.")
	@GetMapping("/{newsId}")
	public ApiResult<PartnerNewsResponse.Detail> detail(
		@Parameter(hidden = true) @RequestAttribute(PartnerApiKeyFilter.PARTNER_ATTRIBUTE) String partner,
		@PathVariable Long newsId) {

		return ApiResult.success("뉴스 상세", partnerNewsService.detail(partner, newsId));
	}
}
