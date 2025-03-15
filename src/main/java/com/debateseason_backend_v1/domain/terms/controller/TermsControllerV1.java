package com.debateseason_backend_v1.domain.terms.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.terms.service.response.LatestTermsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/terms")
public class TermsControllerV1 {

	private final TermsServiceV1 termsService;

	@GetMapping
	public ApiResult<List<LatestTermsResponse>> getLatestTerms() {

		List<LatestTermsResponse> latestTerms = termsService.getLatestTerms();

		return ApiResult.success(
			"최신 약관 목록 조회가 완료되었습니다.",
			latestTerms
		);
	}

	// TODO: 내 약관 동의일자 조회 API

	// TODO: 약관 동의 API

}
