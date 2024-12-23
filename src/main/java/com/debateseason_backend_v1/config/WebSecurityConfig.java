package com.debateseason_backend_v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.debateseason_backend_v1.security.error.AuthenticationErrorHandler;
import com.debateseason_backend_v1.security.jwt.JwtAuthenticationFilter;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final JwtUtil jwtUtil;
	private final AuthenticationErrorHandler errorHandler;

	private static final String[] PUBLIC_URLS = {
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/actuator/**",
		"/ws-stomp/**",
		"/api/v1/**"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				// .anyRequest().permitAll()
				.requestMatchers(PUBLIC_URLS).permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(
				new JwtAuthenticationFilter(jwtUtil, errorHandler),
				UsernamePasswordAuthenticationFilter.class
			)
			.build();
	}

}