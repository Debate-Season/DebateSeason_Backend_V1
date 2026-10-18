package com.debateseason_backend_v1.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.debateseason_backend_v1.common.swagger.ApiErrorCode;
import com.debateseason_backend_v1.common.swagger.ExampleHolder;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * API 문서. 세 그룹으로 나눈다 — 고객사는 자기 그룹만 볼 수 있다 ({@link DocsSecurityConfig}).
 * <ul>
 *   <li>토론철 서비스 — 앱·웹이 쓰는 API</li>
 *   <li>토론철 관리 — /api/v1/admin (ADMIN·서비스 키: 데이터 파이프라인·운영)</li>
 *   <li>티머니모빌리티 — /api/partner (B2B, 고객사 API 키)</li>
 * </ul>
 */
@Configuration
public class SwaggerConfig {

	public static final String GROUP_SERVICE = "toronchul";
	public static final String GROUP_ADMIN = "admin";
	public static final String GROUP_PARTNER = "partner";
	// 지금 고객사는 티머니모빌리티(티머니GO) 하나다. 고객사가 늘면 그룹을 고객사별로 나눈다.
	public static final String PARTNER_DISPLAY_NAME = "티머니모빌리티";
	// 고객사에게 주는 문서 주소. 고객사 그룹으로 바로 연다.
	public static final String PARTNER_DOCS_PATH = "/partner/docs";

	private static final String ADMIN_PATHS = "/api/v1/admin/**";
	private static final String PARTNER_PATHS = "/api/partner/**";

	@Bean
	public GroupedOpenApi serviceApi() {
		return GroupedOpenApi.builder()
			.group(GROUP_SERVICE)
			.displayName("토론철 서비스")
			.pathsToMatch("/api/**")
			.pathsToExclude(ADMIN_PATHS, PARTNER_PATHS)
			.addOpenApiCustomizer(api -> api.info(new Info().title("토론철 서비스 API").version("v1")
				.description("앱·웹이 쓰는 API. 로그인 사용자는 JWT(Authorization: Bearer).")))
			.build();
	}

	@Bean
	public GroupedOpenApi adminApi() {
		return GroupedOpenApi.builder()
			.group(GROUP_ADMIN)
			.displayName("토론철 관리")
			.pathsToMatch(ADMIN_PATHS)
			.addOpenApiCustomizer(api -> {
				api.info(new Info().title("토론철 관리 API").version("v1")
					.description("ADMIN JWT 또는 서비스 키(X-Service-Key). 데이터 파이프라인·운영용. "
						+ "서비스 키는 허용된 경로에서만 통하고, 열리지 않은 경로는 403 SERVICE_KEY_ROUTE_NOT_ALLOWED."));
				api.getComponents().addSecuritySchemes(SERVICE_KEY, new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-Service-Key"));
				api.addSecurityItem(new SecurityRequirement().addList(SERVICE_KEY));
			})
			.build();
	}

	@Bean
	public GroupedOpenApi partnerApi() {
		return GroupedOpenApi.builder()
			.group(GROUP_PARTNER)
			.displayName(PARTNER_DISPLAY_NAME)
			.pathsToMatch(PARTNER_PATHS)
			.addOpenApiCustomizer(api -> {
				api.info(new Info().title("토론철 뉴스 API (티머니모빌리티)").version("v1")
					.description("고객사 전용 API. 발급받은 API 키(X-Partner-Key)로 호출합니다."));
				api.getComponents().addSecuritySchemes(PARTNER_KEY, new SecurityScheme()
					.type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-Partner-Key"));
				// 고객사 문서에는 내부 인증(JWT)을 보이지 않는다
				api.setSecurity(new java.util.ArrayList<>(List.of(new SecurityRequirement().addList(PARTNER_KEY))));
				api.getComponents().getSecuritySchemes().remove("JWT");
			})
			.build();
	}

	private static final String SERVICE_KEY = "ServiceKey";
	private static final String PARTNER_KEY = "PartnerKey";

	@Bean
	public OpenAPI openAPI() {
		String jwt = "JWT";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
		Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
			.name(jwt)
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
		);

		Info customInfo = new Info()
			.title("토론철 API")
			.version("v1")
			.description("토론철 API 문서");

		return new OpenAPI()
			.components(components)
			.addSecurityItem(securityRequirement)
			.info(customInfo);
	}

	@Bean
	public OperationCustomizer customize() {

		return (Operation operation, HandlerMethod handlerMethod) -> {
			ApiErrorCode apiErrorCode = handlerMethod.getMethodAnnotation(
				ApiErrorCode.class
			);

			// @ApiErrorCode 어노테이션이 붙어있다면
			if (apiErrorCode != null) {
				generateErrorCodeResponseExample(operation, apiErrorCode.value());
			}

			return operation;
		};
	}

	// 여러 개의 에러 응답값 추가
	private void generateErrorCodeResponseExample(Operation operation, ErrorCode[] errorCodes) {

		ApiResponses responses = operation.getResponses();

		// ExampleHolder(에러 응답값) 객체를 만들고 에러 코드별로 그룹화
		Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorCodes)
			.map(
				errorCode -> ExampleHolder.builder()
					.holder(getSwaggerExample(errorCode))
					.code(errorCode.getHttpStatus().value())
					.name(errorCode.name())
					.build()
			)
			.collect(Collectors.groupingBy(ExampleHolder::getCode));

		// ExampleHolders를 ApiResponses에 추가
		addExamplesToResponses(responses, statusWithExampleHolders);
	}

	// ErrorResponse 형태의 예시 객체 생성
	private Example getSwaggerExample(ErrorCode errorCode) {

		ErrorResponse errorResponseDto = ErrorResponse.of(errorCode);
		Example example = new Example();
		example.setValue(errorResponseDto);

		return example;
	}

	// exampleHolder를 ApiResponses에 추가
	private void addExamplesToResponses(
		ApiResponses responses,
		Map<Integer, List<ExampleHolder>> statusWithExampleHolders
	) {

		statusWithExampleHolders.forEach(
			(status, v) -> {
				Content content = new Content();
				MediaType mediaType = new MediaType();
				ApiResponse apiResponse = new ApiResponse();

				v.forEach(
					exampleHolder -> mediaType.addExamples(
						exampleHolder.getName(),
						exampleHolder.getHolder()
					)
				);
				content.addMediaType("application/json", mediaType);
				apiResponse.setContent(content);
				responses.addApiResponse(String.valueOf(status), apiResponse);
			}
		);
	}
}
