package com.debateseason_backend_v1.domain.user.controller.docs;

import org.springframework.web.bind.annotation.RequestBody;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.swagger.ApiErrorCode;
import com.debateseason_backend_v1.domain.user.controller.request.OidcLoginRequest;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User API V2", description = "계정 관리 API V2")
public interface UserControllerV2Docs {

	@Operation(
		summary = "로그인",
		description = """
			사용자를 로그인 시킵니다. \n
			첫 로그인(프로필 미등록)인 경우 `profileStatus`가 `false`, \n
			프로필 등록 후 로그인의 경우 `profileStatus`가 `true`로 응답됩니다.
			"""
	)
	@ApiResponse(responseCode = "200", description = "로그인 성공")
	@ApiErrorCode({
		ErrorCode.MISSING_REQUIRED_SOCIAL_TYPE,
		ErrorCode.MISSING_REQUIRED_ID_TOKEN,
		ErrorCode.NOT_SUPPORTED_SOCIAL_TYPE,
		ErrorCode.INVALID_JWKS_URL,
		ErrorCode.ID_TOKEN_SIGNATURE_VALIDATION_FAILED,
		ErrorCode.ID_TOKEN_DECODING_FAILED,
		ErrorCode.JWKS_NETWORK_ERROR,
		ErrorCode.NOT_FOUND_JWKS_KEY,
		ErrorCode.JWKS_RATE_LIMIT_REACHED,
		ErrorCode.PUBLIC_KEY_EXTRACTION_FAILED,
		ErrorCode.JWKS_RETRIEVAL_FAILED,
	})
	public ApiResult<LoginResponse> login(@RequestBody OidcLoginRequest request);
}
