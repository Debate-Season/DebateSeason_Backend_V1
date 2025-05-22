package com.debateseason_backend_v1.domain.terms.controller.docs;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.common.swagger.ApiErrorCode;
import com.debateseason_backend_v1.domain.terms.controller.request.TermsAgreementRequest;
import com.debateseason_backend_v1.domain.terms.service.response.LatestTermsResponse;
import com.debateseason_backend_v1.domain.terms.service.response.UserTermsAgreementResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Terms API", description = "이용약관 API")
public interface TermsControllerV1Docs {

	@Operation(
		summary = "최신 이용약관 목록 조회",
		description = "최신 버전의 이용약관들을 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "이용약관 목록 조회 성공")
	public ApiResult<List<LatestTermsResponse>> getLatestTerms(CustomUserDetails userDetails);

	@Operation(
		summary = "이용약관 동의",
		description = "이용약관의 동의 항목을 저장하는 API"
	)
	@ApiResponse(responseCode = "200", description = "이용약관 동의 성공")
	@ApiErrorCode({
		ErrorCode.MISSING_ACCESS_TOKEN,
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN,
		ErrorCode.NOT_FOUND_TERMS,
		ErrorCode.REQUIRED_TERMS_NOT_AGREED,
	})
	public VoidApiResult agree(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody TermsAgreementRequest request
	);

	@Operation(
		summary = "내가 동의한 이용약관 목록 조회",
		description = "내가 동의한 이용약관 목록을 조회하는 API"
	)
	@ApiErrorCode({
		ErrorCode.MISSING_ACCESS_TOKEN,
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN
	})
	@GetMapping("/agree")
	public ApiResult<List<UserTermsAgreementResponse>> getUserTermsInfo(
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

}
