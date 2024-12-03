package com.debateseason_backend_v1.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.security.dto.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private static final String COOKIE_NAME = "JWT_TOKEN";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		try {
			String token = extractTokenFromCookies(request);

			if (token == null) {
				filterChain.doFilter(request, response);
				return;
			}

			if (isValidToken(token, response)) {
				authenticateUser(token);
				filterChain.doFilter(request, response);
			} else {
				// 토큰이 유효하지 않은 경우 에러 응답
				handleInvalidToken(response, "Invalid or expired token");
			}

		} catch (ExpiredJwtException e) {
			log.error("Token expired", e);
			removeJwtCookie(response);
			handleInvalidToken(response, "Token has expired");
		} catch (Exception e) {
			log.error("JWT authentication failed ", e);
			handleInvalidToken(response, "Authentication failed");
		}
	}

	private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
		errorResponse.put("message", message);

		new ObjectMapper().writeValue(response.getWriter(), errorResponse);
	}

	private boolean isValidToken(String token, HttpServletResponse response) {

		try {
			return !jwtUtil.isExpired(token);
		} catch (ExpiredJwtException e) {
			log.info("Token expired");
			removeJwtCookie(response);
			return false;
		}
	}

	private String extractTokenFromCookies(HttpServletRequest request) {

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			log.debug("No cookies found");
			return null;
		}

		return Arrays.stream(cookies)
			.filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElseGet(() -> {
				log.debug("JWT token not found in cookies");
				return null;
			});
	}

	private void authenticateUser(String token) {

		String username = jwtUtil.getUsername(token);
		String role = jwtUtil.getRole(token);

		User user = User.builder()
			.username(username)
			.password("temppassword")
			.role(role)
			.build();

		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		Authentication authToken = new UsernamePasswordAuthenticationToken(
			customUserDetails,
			null,
			customUserDetails.getAuthorities()
		);

		SecurityContextHolder.getContext().setAuthentication(authToken);
	}

	private void removeJwtCookie(HttpServletResponse response) {

		Cookie cookie = new Cookie(COOKIE_NAME, null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		response.addCookie(cookie);
	}

}
