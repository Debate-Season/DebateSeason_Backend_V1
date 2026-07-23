package com.debateseason_backend_v1.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.security.CustomUserDetails;
import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
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
	private final SecurityPathMatcher securityPathMatcher;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String requestURI = request.getRequestURI();

		if (securityPathMatcher.isPublicUrl(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		// Optional Auth: 헤더가 없으면 익명으로 통과, 헤더가 있으면 토큰 유효성을 강제한다.
		// "헤더 없음(진짜 비로그인)" 과 "토큰은 있으나 만료/무효" 를 구분해, 후자는 익명으로
		// 조용히 강등하지 않고 401 을 돌려준다. 그래야 클라이언트의 401 기반 리프레시가 동작한다.
		if (securityPathMatcher.isOptionalAuthUrl(request)) {
			String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
			if (containsValidHeader(authorizationHeader)
				&& !authenticateFromHeader(authorizationHeader, response, requestURI)) {
				// 헤더는 있으나 토큰이 만료/무효 -> 401 을 이미 기록했으므로 체인 중단
				return;
			}
			// 헤더가 없으면 순수 비로그인 -> 익명 응답 유지
			filterChain.doFilter(request, response);
			return;
		}

		// Authorization 헤더 검증 (필수 인증)
		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

		if (!containsValidHeader(authorizationHeader)) {
			log.debug("JWT 토큰이 없습니다, uri: {}", requestURI);
			errorHandler.handleMissingToken(response, requestURI);
			return;
		}

		if (!authenticateFromHeader(authorizationHeader, response, requestURI)) {
			return;
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * 헤더에 실린 Bearer 토큰을 검증해 SecurityContext 에 인증을 세팅한다.
	 * 성공하면 {@code true}, 실패하면 401(만료/무효)을 응답에 기록하고 {@code false} 를 반환한다.
	 * (호출 측은 {@code false} 면 필터 체인을 중단해야 한다.)
	 *
	 * <p>검증 실패 처리는 필수 인증과 optional 인증이 공유한다 -> 두 경로의 정책이 항상 일치한다.
	 * 인증 처리만 try 로 감싸고 {@code filterChain.doFilter} 는 호출 측에 두므로,
	 * 다운스트림 컨트롤러가 던진 예외를 여기서 삼켜 401 로 오인하는 일은 없다.
	 */
	private boolean authenticateFromHeader(
		String authorizationHeader,
		HttpServletResponse response,
		String requestURI
	) throws IOException {

		String token = removeBearerPrefix(authorizationHeader);
		if (token == null) {
			log.debug("유효한 JWT 토큰 형식이 아닙니다, uri: {}", requestURI);
			errorHandler.handleInvalidToken(response, requestURI);
			return false;
		}

		try {
			authenticateWithAccessToken(token, requestURI);
			return true;
		} catch (ExpiredJwtException e) {
			errorHandler.handleExpiredToken(response, requestURI);
			return false;
		} catch (JwtException | IllegalArgumentException e) {
			errorHandler.handleInvalidToken(response, requestURI);
			return false;
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
		CustomUserDetails principal = CustomUserDetails.from(userId, jwtUtil.getRole(token));

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

}