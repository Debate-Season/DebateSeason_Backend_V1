package com.debateseason_backend_v1.security.component;

import static com.debateseason_backend_v1.config.WebSecurityConfig.OPTIONAL_AUTH_URLS;
import static com.debateseason_backend_v1.config.WebSecurityConfig.PUBLIC_URLS;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityPathMatcher {

	private final PathMatcher pathMatcher;

	public SecurityPathMatcher() {
		this.pathMatcher = new AntPathMatcher();
	}

	public boolean isPublicUrl(HttpServletRequest request) {
		String path = resolvePath(request);
		return Arrays.stream(PUBLIC_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	// 조회는 비로그인에 열려 있지만 생성은 ADMIN 전용인 경로들.
	// POST 를 Optional 로 두면 비로그인 요청이 인가 단계까지 흘러가 403 이 되는데,
	// "로그인이 필요하다"는 사실을 알리는 401 이 더 정확하다.
	private static final String[] GET_ONLY_OPTIONAL_URLS = {
		"/api/v1/room",
		"/api/v1/issue"
	};

	public boolean isOptionalAuthUrl(HttpServletRequest request) {
		String path = resolvePath(request);

		boolean getOnly = Arrays.stream(GET_ONLY_OPTIONAL_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
		if (getOnly) {
			return "GET".equalsIgnoreCase(request.getMethod());
		}

		return Arrays.stream(OPTIONAL_AUTH_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	public String resolvePath(HttpServletRequest request) {
		// getServletPath()는 context-path가 이미 제거된 경로를 반환
		String servletPath = request.getServletPath();
		if (servletPath != null && !servletPath.isEmpty()) {
			return servletPath;
		}

		// fallback: 수동으로 context-path 제거
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
			return uri.substring(contextPath.length());
		}
		return uri;
	}

}