package com.debateseason_backend_v1.security.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
 * <p>키는 {@link #SERVICE_KEY_PATCH_URLS} 에서만 받는다. 다른 경로에 실린 헤더는 무시하고 평소 JWT 규칙을 따른다.
 * 키 하나가 새도 피해가 이 경로들로 묶이게 하려는 것이다.
 *
 * <p>헤더가 있는데 키가 틀리면 JWT 로 넘기지 않고 바로 401 이다. 넘기면 "토큰이 없다" 는 엉뚱한 오류가 나서
 * 파이프라인 쪽에서 키 문제인지 알 수 없다.
 */
@RequiredArgsConstructor
public class ServiceApiKeyFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Service-Key";

	// 서비스 키를 받는 경로 (PATCH 만). WebSecurityConfig 의 인가 규칙과 같은 목록을 쓴다.
	public static final String[] SERVICE_KEY_PATCH_URLS = {
		"/api/v1/admin/issue/*/status"
	};

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
		if (!"PATCH".equalsIgnoreCase(request.getMethod())) {
			return false;
		}
		String path = securityPathMatcher.resolvePath(request);
		for (String pattern : SERVICE_KEY_PATCH_URLS) {
			if (PATH_MATCHER.match(pattern, path)) {
				return true;
			}
		}
		return false;
	}
}
