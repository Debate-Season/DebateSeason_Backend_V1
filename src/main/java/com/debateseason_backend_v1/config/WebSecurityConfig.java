package com.debateseason_backend_v1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.error.JwtAccessDeniedHandler;
import com.debateseason_backend_v1.security.error.JwtAuthenticationErrorHandler;
import com.debateseason_backend_v1.security.filter.ClientInfoFilter;
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
	private final JwtAccessDeniedHandler accessDeniedHandler;
	private final SecurityPathMatcher securityPathMatcher;
	private final ObjectMapper objectMapper;

	@Value("${rate-limit.anonymous-requests-per-minute:100}")
	private long anonymousRateLimit;

	@Value("${rate-limit.authenticated-requests-per-minute:300}")
	private long authenticatedRateLimit;

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

		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			// 인증(로그인 했는가)은 JwtAuthenticationFilter 가, 인가(권한이 있는가)는 여기가 담당한다.
			// anyRequest() 를 한 번에 authenticated() 로 바꾸면 회귀 범위가 너무 넓어지므로
			// ADMIN 전용 경로만 먼저 잠그고 나머지는 기존대로 둔다. (PRD v1.3.4 §4.3)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/v1/issue").hasRole("ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/v1/room").hasRole("ADMIN")
				.anyRequest().permitAll()
			)
			.exceptionHandling(handler -> handler.accessDeniedHandler(accessDeniedHandler))
			// addFilterBefore 의 앵커는 Spring Security 가 아는 필터여야 하므로
			// 커스텀 필터(JwtAuthenticationFilter)를 앵커로 쓸 수 없다.
			// 같은 앵커에 등록하면 등록 순서가 유지되므로 ClientInfoFilter 를 먼저 등록한다.
			.addFilterBefore(new ClientInfoFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class)
			.build();
	}
}
