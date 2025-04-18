package com.debateseason_backend_v1.domain.auth.controller.docs;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.swagger.ApiErrorCode;
import com.debateseason_backend_v1.domain.auth.controller.request.TokenReissueRequest;
import com.debateseason_backend_v1.domain.auth.service.response.TokenReissueResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth API", description = "인증 API")
public interface AuthControllerV1Docs {

	@Operation(
		summary = "토큰 재발급",
		description = "`Refresh Token`을 사용하여 새로운 `Access Token`과 `Refresh Token`을 발급합니다."
	)
	@ApiResponse(responseCode = "200", description = "`Access Token`, `Refresh Token` 재발급 성공")
	@ApiErrorCode({
		ErrorCode.EXPIRED_REFRESH_TOKEN,
		ErrorCode.INVALID_REFRESH_TOKEN,
		ErrorCode.MISSING_REFRESH_TOKEN
	})
	ApiResult<TokenReissueResponse> reissueToken(@RequestBody TokenReissueRequest request);
}