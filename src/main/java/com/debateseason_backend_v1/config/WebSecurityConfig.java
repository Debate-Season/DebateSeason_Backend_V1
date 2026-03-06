package com.debateseason_backend_v1.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.error.JwtAuthenticationErrorHandler;
import com.debateseason_backend_v1.security.filter.RateLimitFilter;
import com.debateseason_backend_v1.security.jwt.JwtAuthenticationFilter;
import com.debateseason_backend_v1.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final JwtUtil jwtUtil;
	private final JwtAuthenticationErrorHandler errorHandler;
	private final SecurityPathMatcher securityPathMatcher;
	private final ObjectMapper objectMapper;

	@Value("${rate-limit.anonymous-requests-per-minute:100}")
	private long anonymousRateLimit;

	@Value("${rate-limit.authenticated-requests-per-minute:300}")
	private long authenticatedRateLimit;

	private static RequestMatcher[] toAntMatchers(String[] patterns) {
		return Arrays.stream(patterns)
			.map(AntPathRequestMatcher::new)
			.toArray(RequestMatcher[]::new);
	}

	public static final String[] PUBLIC_URLS = {
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/actuator/**",
		"/ws-stomp/**",
		"/stomp/**",
		"/topic/**",
		"/api/v1/users/login",
		"/api/v2/users/login",
		"/api/v1/auth/reissue",
		"/api/v1/app/**"
	};

	public static final String[] OPTIONAL_AUTH_URLS = {
		"/api/v1/issue",
		"/api/v1/issue-map",
		"/api/v1/home/recommend",
		"/api/v1/home/media",
		"/api/v1/room",
		"/api/v1/users/home"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, errorHandler, securityPathMatcher);
		RateLimitFilter rateLimitFilter = new RateLimitFilter(
			securityPathMatcher, objectMapper, anonymousRateLimit, authenticatedRateLimit
		);

		RequestMatcher[] publicMatchers = toAntMatchers(PUBLIC_URLS);
		RequestMatcher[] optionalMatchers = toAntMatchers(OPTIONAL_AUTH_URLS);

		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(publicMatchers).permitAll()
				.requestMatchers(optionalMatchers).permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class)
			.build();
	}
}
