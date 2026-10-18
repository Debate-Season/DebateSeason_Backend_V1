package com.debateseason_backend_v1.security.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.error.JwtAuthenticationErrorHandler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * {@code X-Service-Key} 헤더로 서비스 호출자를 인증한다. JwtAuthenticationFilter 보다 먼저 돈다.
 *
 * <p>키는 {@link #ROUTES} 에서만 받는다 (메서드까지 맞아야 한다). 다른 경로에 실린 헤더는 무시하고
 * 평소 JWT 규칙을 따른다. 키 하나가 새도 피해가 온톨로지 반영 API 로 묶이게 하려는 것이다.
 *
 * <p>헤더가 있는데 키가 틀리면 JWT 로 넘기지 않고 바로 401 이다. 넘기면 "토큰이 없다" 는 엉뚱한 오류가 나서
 * 파이프라인 쪽에서 키 문제인지 알 수 없다.
 */
@RequiredArgsConstructor
public class ServiceApiKeyFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Service-Key";

	public record Route(HttpMethod method, String pattern) {
	}

	// 서비스 키를 받는 경로. WebSecurityConfig 의 인가 규칙이 같은 목록을 쓴다.
	// 경로를 늘릴 때는 여기만 고치면 인증·인가가 함께 열린다.
	public static final List<Route> ROUTES = List.of(
		new Route(HttpMethod.POST, "/api/v1/admin/issue"),
		new Route(HttpMethod.PATCH, "/api/v1/admin/issue/*"),
		new Route(HttpMethod.PATCH, "/api/v1/admin/issue/*/status"),
		new Route(HttpMethod.POST, "/api/v1/admin/issue/*/debate-topic"),
		new Route(HttpMethod.POST, "/api/v1/admin/issue/*/events"),
		new Route(HttpMethod.DELETE, "/api/v1/admin/issue/*/events"),
		new Route(HttpMethod.PATCH, "/api/v1/admin/room/*/status"),
		new Route(HttpMethod.PATCH, "/api/v1/admin/event/*"),
		new Route(HttpMethod.POST, "/api/v1/admin/issue/*/events/merge"),
		new Route(HttpMethod.POST, "/api/v1/admin/change-log/*/revert"),
		// 읽기 전용 단건 조회. 파이프라인이 자기 기록과 운영 타임라인을 맞춰 보는 데 쓴다.
		new Route(HttpMethod.GET, "/api/v1/admin/issue/*/timeline/versions"),
		// 위키는 DRAFT 업로드만. 게시(/publish)는 사람(ADMIN)만 한다 — 자동 게시 금지.
		new Route(HttpMethod.POST, "/api/v1/admin/wiki/drafts")
	);

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	private final ServiceApiKeys serviceApiKeys;
	private final SecurityPathMatcher securityPathMatcher;
	private final JwtAuthenticationErrorHandler errorHandler;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String presented = request.getHeader(HEADER);
		if (presented == null || !acceptsServiceKey(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		Optional<String> serviceName = serviceApiKeys.resolve(presented);
		if (serviceName.isEmpty()) {
			errorHandler.handleInvalidServiceKey(response, request.getRequestURI());
			return;
		}

		ServicePrincipal principal = new ServicePrincipal(serviceName.get());
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
			principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + ServicePrincipal.ROLE))
		));
		filterChain.doFilter(request, response);
	}

	private boolean acceptsServiceKey(HttpServletRequest request) {
		String path = securityPathMatcher.resolvePath(request);
		return ROUTES.stream().anyMatch(route ->
			route.method().matches(request.getMethod()) && PATH_MATCHER.match(route.pattern(), path));
	}
}
