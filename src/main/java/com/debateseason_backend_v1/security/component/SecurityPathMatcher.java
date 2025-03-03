package com.debateseason_backend_v1.security.component;

import static com.debateseason_backend_v1.config.WebSecurityConfig.*;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityPathMatcher {

	private final PathMatcher pathMatcher = new AntPathMatcher();

	public boolean isPublicUrl(String requestURI) {
		return Arrays.stream(PUBLIC_URLS)
			.anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
	}

}