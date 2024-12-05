package com.debateseason_backend_v1.domain.auth.doc;

import com.debateseason_backend_v1.security.dto.LoginRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthControllerDoc {

	@Operation(summary = "로그인")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "로그인 성공",
			headers = @Header(
				name = "Set-Cookie",
				description = "JWT_TOKEN={token}",
				schema = @Schema(
					type = "string",
					example = "JWT_TOKEN=eyJhbGciOiJIUzI1NiJ9...; Path=/; HttpOnly; Max-Age=2592000"
				)
			),
			content = @Content(mediaType = "application/json", examples = @ExampleObject(
				value = """
					{
					    "status": 200,
					    "code": "SUCCESS",
					    "message": "로그인 성공",
					    "data": {
					        "username": "jongin",
					        "role": "ROLE_USER"
					    }
					}
					"""))
		),
		@ApiResponse(
			responseCode = "401",
			description = "로그인 실패 - 잘못된 인증 정보",
			content = @Content(mediaType = "application/json", examples = @ExampleObject(
				value = """
					{
					    "status": 401,
					    "code": "INVALID_CREDENTIALS",
					    "message": "아이디 또는 비밀번호가 올바르지 않습니다."
					}
					"""))
		)
	})
	public void login(LoginRequestDTO loginRequestDTO);
	
}
