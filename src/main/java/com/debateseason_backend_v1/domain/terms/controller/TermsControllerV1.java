package com.debateseason_backend_v1.domain.terms.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.terms.controller.docs.TermsControllerV1Docs;
import com.debateseason_backend_v1.domain.terms.controller.request.TermsAgreementRequest;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.terms.service.response.LatestTermsResponse;
import com.debateseason_backend_v1.domain.terms.service.response.UserTermsAgreementResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/terms")
public class TermsControllerV1 implements TermsControllerV1Docs {

	private final TermsServiceV1 termsService;

	@GetMapping
	public ApiResult<List<LatestTermsResponse>> getLatestTerms(@AuthenticationPrincipal CustomUserDetails userDetails) {

		List<LatestTermsResponse> latestTerms = termsService.getLatestTerms(userDetails.getUserId());

		return ApiResult.success(
			"최신 약관 목록 조회가 완료되었습니다.",
			latestTerms
		);
	}

	@PostMapping("/agree")
	public VoidApiResult agree(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody TermsAgreementRequest request
	) {

		termsService.agree(request.toServiceRequest(userDetails.getUserId()));

		return VoidApiResult.success("약관 동의에 성공했습니다.");
	}

	@GetMapping("/agree")
	public ApiResult<List<UserTermsAgreementResponse>> getUserTermsInfo(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		List<UserTermsAgreementResponse> responses = termsService.getUserTermsInfo(userDetails.getUserId());

		return ApiResult.success("이용약관 동의 목록 조회에 성공했습니다.", responses);
	}
}
