package com.debateseason_backend_v1.domain.profile.controller.docs;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.profile.controller.request.ProfileRegisterRequest;
import com.debateseason_backend_v1.domain.profile.controller.request.ProfileUpdateRequest;
import com.debateseason_backend_v1.domain.profile.controller.request.request.ProfileRegisterRequest;
import com.debateseason_backend_v1.domain.profile.controller.request.request.ProfileUpdateRequest;
import com.debateseason_backend_v1.domain.profile.service.response.ProfileResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Profile API", description = "프로필 관련 API")
public interface ProfileControllerV1Docs {

	@Operation(
		summary = "프로필 등록",
		description = """
			✅ API를 사용하려면 Access Token이 필요합니다. \n
			JWT 토큰에서 사용자 ID를 추출하여 프로필을 등록합니다.
			""",
		security = @SecurityRequirement(name = "JWT")
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "프로필 등록 정보",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ProfileRegisterRequest.class),
			examples = @ExampleObject(
				value = """
					{
					    "nickname": "홍길동",
					    "communityId": 1,
					    "gender": "남성",
					    "ageRange": "20대"
					}
					"""
			)
		)
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "프로필 등록 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResult.class),
				examples = @ExampleObject(
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "프로필 등록이 완료되었습니다."
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "MissingSingleRequiredValue",
						summary = "필수 입력값에 빈 문자열 입력",
						value = """
							{
							    "status": 400,
							    "code": "MISSING_REQUIRED_VALUE",
							    "message": "닉네임은 필수입니다."
							}
							"""
					),
					@ExampleObject(
						name = "MissingMultipleRequiredValue",
						summary = "필수 입력값에 빈 문자열 입력(여러 개)",
						value = """
							{
							    "status": 400,
							    "code": "MISSING_REQUIRED_VALUE",
							    "message": "닉네임은 필수입니다., 커뮤니티 선택은 필수입니다."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidNickname",
						summary = "잘못된 닉네임 형식",
						value = """
							{
							    "status": 400,
							    "code": "INVALID_NICKNAME_FORMAT",
							    "message": "닉네임은 한글 또는 영문으로 8자 이내로 입력해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidGenderType",
						summary = "잘못된 성별 타입",
						value = """
							{
							    "status": 400,
							    "code": "NOT_SUPPORTED_GENDER_TYPE",
							    "message": "지원하지 않는 성별 타입입니다"
							}
							"""
					),
					@ExampleObject(
						name = "InvalidAgeRange",
						summary = "잘못된 연령대",
						value = """
							{
							    "status": 400,
							    "code": "NOT_SUPPORTED_AGE_RANGE",
							    "message": "지원하지 않는 연령대입니다"
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증 실패",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					oneOf = {
						ErrorResponse.class
					}
				),
				examples = {
					@ExampleObject(
						name = "ExpiredAccessToken",
						summary = "만료된 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "EXPIRED_ACCESS_TOKEN",
							    "message": "Access Token이 만료되었습니다. 다시 로그인해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidAccessToken",
						summary = "유효하지 않은 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "INVALID_ACCESS_TOKEN",
							    "message": "유효하지 않은 Access Token 입니다."
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "중복된 닉네임",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 409,
						    "code": "DUPLICATE_NICKNAME",
						    "message": "중복된 닉네임입니다."
						}
						"""
				)
			)
		)
	})
	public VoidApiResult registerProfile(
		@RequestBody ProfileRegisterRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "내 프로필 조회",
		description = """
			✅ API를 사용하려면 Access Token이 필요합니다. \n
			JWT 토큰에서 사용자 ID를 추출하여 자신의 프로필을 조회합니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "프로필 조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					oneOf = {
						ApiResult.class,
						ProfileResponse.class
					},
					description = "로그인 응답 데이터"
				),
				examples = @ExampleObject(
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "프로필 조회가 완료되었습니다.",
						    "data": {
						        "nickname": "홍길동",
						        "gender": "남성",
						        "ageRange": "20대",
						        "community": {
						            "id": 1,
						            "name": "디시",
						            "iconUrl": "community/icons/dcinside.png"
						        }
						    }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증 실패",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					oneOf = {
						ErrorResponse.class
					}
				),
				examples = {
					@ExampleObject(
						name = "ExpiredAccessToken",
						summary = "만료된 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "EXPIRED_ACCESS_TOKEN",
							    "message": "Access Token이 만료되었습니다. 다시 로그인해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidAccessToken",
						summary = "유효하지 않은 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "INVALID_ACCESS_TOKEN",
							    "message": "유효하지 않은 Access Token 입니다."
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "프로필을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 404,
						    "code": "NOT_EXIST_PROFILE",
						    "message": "프로필이 존재하지 않습니다."
						}
						"""
				)
			)
		)
	})
	public ApiResult<ProfileResponse> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "프로필 수정",
		description = """
			✅ API를 사용하려면 Access Token이 필요합니다. \n
			JWT 토큰에서 사용자 ID를 추출하여 프로필을 수정합니다.
			"""
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		description = "프로필 수정 정보",
		required = true,
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ProfileUpdateRequest.class),
			examples = @ExampleObject(
				value = """
					{
					    "nickname": "홍길동",
					    "communityId": 2,
					    "gender": "남성",
					    "ageRange": "30대"
					}
					"""
			)
		)
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "프로필 수정 성공",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "프로필 수정이 완료되었습니다."
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "MissingSingleRequiredValue",
						summary = "필수 입력값에 빈 문자열 입력",
						value = """
							{
							    "status": 400,
							    "code": "MISSING_REQUIRED_VALUE",
							    "message": "닉네임은 필수입니다."
							}
							"""
					),
					@ExampleObject(
						name = "MissingMultipleRequiredValue",
						summary = "필수 입력값에 빈 문자열 입력(여러 개)",
						value = """
							{
							    "status": 400,
							    "code": "MISSING_REQUIRED_VALUE",
							    "message": "닉네임은 필수입니다., 커뮤니티 선택은 필수입니다."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidNickname",
						summary = "잘못된 닉네임 형식",
						value = """
							{
							    "status": 400,
							    "code": "INVALID_NICKNAME_FORMAT",
							    "message": "닉네임은 한글 또는 영문으로 8자 이내로 입력해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidAgeRange",
						summary = "잘못된 연령대",
						value = """
							{
							    "status": 400,
							    "code": "NOT_SUPPORTED_AGE_RANGE",
							    "message": "지원하지 않는 연령대입니다"
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증 실패",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					oneOf = {
						ErrorResponse.class
					}
				),
				examples = {
					@ExampleObject(
						name = "ExpiredAccessToken",
						summary = "만료된 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "EXPIRED_ACCESS_TOKEN",
							    "message": "Access Token이 만료되었습니다. 다시 로그인해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidAccessToken",
						summary = "유효하지 않은 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "INVALID_ACCESS_TOKEN",
							    "message": "유효하지 않은 Access Token 입니다."
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "프로필을 찾을 수 없음",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 404,
						    "code": "NOT_EXIST_PROFILE",
						    "message": "프로필이 존재하지 않습니다."
						}
						"""
				)
			)
		)
	})
	public VoidApiResult updateProfile(
		@RequestBody ProfileUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "닉네임 중복 확인",
		description = "닉네임의 중복 여부를 확인합니다."
	)
	@Parameter(
		name = "query",
		description = "중복 확인할 닉네임",
		required = true,
		example = "홍길동",
		schema = @Schema(type = "string")
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "닉네임 사용 가능",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "사용 가능한 닉네임입니다."
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 닉네임 형식",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 400,
						    "code": "INVALID_NICKNAME_FORMAT",
						    "message": "닉네임은 한글 또는 영문으로 8자 이내로 입력해주세요."
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "409",
			description = "중복된 닉네임",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 409,
						    "code": "DUPLICATE_NICKNAME",
						    "message": "중복된 닉네임입니다."
						}
						"""
				)
			)
		)
	})
	public VoidApiResult checkNicknameDuplicate(
		@RequestParam String query
	);
}