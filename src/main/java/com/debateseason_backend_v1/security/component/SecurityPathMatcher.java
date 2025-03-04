package com.debateseason_backend_v1.security.component;

import static com.debateseason_backend_v1.config.WebSecurityConfig.*;

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

		String pathWithoutContext = requestURI;
		if (!contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
			pathWithoutContext = requestURI.substring(contextPath.length());
		}

		// 최종 값을 final 변수에 할당
		final String finalPath = pathWithoutContext;

		return Arrays.stream(PUBLIC_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, finalPath));
	}

}