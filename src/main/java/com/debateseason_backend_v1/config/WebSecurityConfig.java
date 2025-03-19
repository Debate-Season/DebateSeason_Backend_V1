package com.debateseason_backend_v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.error.JwtAuthenticationErrorHandler;
import com.debateseason_backend_v1.security.jwt.JwtAuthenticationFilter;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final JwtUtil jwtUtil;
	private final JwtAuthenticationErrorHandler errorHandler;
	private final SecurityPathMatcher securityPathMatcher;

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
		"/api/v1/terms",
		"/api/v1/app/**"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(PUBLIC_URLS).permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(
				new JwtAuthenticationFilter(jwtUtil, errorHandler, securityPathMatcher),
				UsernamePasswordAuthenticationFilter.class
			)
			.build();
	}

}
