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
		String path = stripContextPath(request);
		return Arrays.stream(PUBLIC_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	public boolean isOptionalAuthUrl(HttpServletRequest request) {
		String path = stripContextPath(request);

		// /api/v1/room은 GET만 Optional, POST는 Required Auth
		if (pathMatcher.match("/api/v1/room", path)) {
			return "GET".equalsIgnoreCase(request.getMethod());
		}

		return Arrays.stream(OPTIONAL_AUTH_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	private String stripContextPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
			return uri.substring(contextPath.length());
		}
		return uri;
	}

}