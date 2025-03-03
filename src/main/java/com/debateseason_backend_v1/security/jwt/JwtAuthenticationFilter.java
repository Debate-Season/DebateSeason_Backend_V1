package com.debateseason_backend_v1.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.config.WebSecurityConfig;
import com.debateseason_backend_v1.security.CustomUserDetails;
import com.debateseason_backend_v1.security.error.JwtAuthenticationErrorHandler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BEARER_PREFIX = "Bearer ";
	private final JwtUtil jwtUtil;
	private final JwtAuthenticationErrorHandler errorHandler;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String requestURI = request.getRequestURI();

		if (isPublicUrl(requestURI)) {
			filterChain.doFilter(request, response);
			return;
		}

		// 1. Authorization 헤더 검증
		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (!containsValidHeader(authorizationHeader)) {
			log.debug("JWT 토큰이 없습니다, uri: {}", requestURI);
			errorHandler.handleMissingToken(response, requestURI);
			return;
		}

		// 2. Bearer 토큰 추출
		String token = removeBearerPrefix(authorizationHeader);
		if (token == null) {
			log.debug("유효한 JWT 토큰 형식이 아닙니다, uri: {}", requestURI);
			errorHandler.handleInvalidToken(response, requestURI);
			return;
		}

		// 3. 토큰 유효성 검증 및 인증 처리
		try {
			authenticateWithAccessToken(token, requestURI);
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException e) {
			errorHandler.handleExpiredToken(response, requestURI);
			return;
		} catch (JwtException e) {
			errorHandler.handleInvalidToken(response, requestURI);
			return;
		}
	}

	// Authorization 헤더 존재 여부 확인
	private boolean containsValidHeader(String authorization) {

		return authorization != null && !authorization.isBlank();
	}

	// Bearer 토큰 추출
	private String removeBearerPrefix(String authorization) {

		if (authorization.startsWith(BEARER_PREFIX)) {
			return authorization.substring(7);
		}
		return null;
	}

	private void authenticateWithAccessToken(String token, String requestURI) {
		
		jwtUtil.validate(token);
		TokenType tokenType = jwtUtil.getTokenType(token);
		if (tokenType != TokenType.ACCESS) {
			throw new JwtException("Invalid token type");
		}
		setupUserAuthentication(token, requestURI);
	}

	private void setupUserAuthentication(String token, String requestURI) {

		Long userId = jwtUtil.getUserId(token);
		CustomUserDetails principal = CustomUserDetails.from(userId);

		Authentication authentication =
			new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
			);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		log.debug(
			"Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}",
			authentication.getName(), requestURI
		);
	}

	// 공개 URL 여부 확인
	private boolean isPublicUrl(String requestURI) {

		for (String publicUrl : WebSecurityConfig.PUBLIC_URLS) {
			if (new AntPathMatcher().match(publicUrl, requestURI)) {
				return true;
			}
		}
		return false;
	}

}