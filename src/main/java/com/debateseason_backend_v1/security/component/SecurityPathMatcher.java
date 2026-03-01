package com.debateseason_backend_v1.security.component;

import static com.debateseason_backend_v1.config.WebSecurityConfig.OPTIONAL_AUTH_URLS;
import static com.debateseason_backend_v1.config.WebSecurityConfig.PUBLIC_URLS;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityPathMatcher {

	private final PathMatcher pathMatcher;
	private final String contextPath;

	public SecurityPathMatcher(@Value("${server.servlet.context-path:}") String contextPath) {
		this.pathMatcher = new AntPathMatcher();
		this.contextPath = contextPath;
	}

	public boolean isPublicUrl(String requestURI) {
		String path = removeContextPath(requestURI);
		return Arrays.stream(PUBLIC_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	public boolean isOptionalAuthUrl(String requestURI, String method) {
		String path = removeContextPath(requestURI);

		// /api/v1/room은 GET만 Optional, POST는 Required Auth
		if (pathMatcher.match("/api/v1/room", path)) {
			return "GET".equalsIgnoreCase(method);
		}

		return Arrays.stream(OPTIONAL_AUTH_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	private String removeContextPath(String requestURI) {
		if (!contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
			return requestURI.substring(contextPath.length());
		}
		return requestURI;
	}

}