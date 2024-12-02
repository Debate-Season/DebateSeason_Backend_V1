package com.debateseason_backend_v1.security.jwt;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.security.dto.CustomUserDetails;

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

		String token = extractTokenFromCookies(request);

		if (isValidToken(token, response)) {
			authenticateUser(token);
		}

		filterChain.doFilter(request, response);
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

	private boolean isValidToken(String token, HttpServletResponse response) {

		if (token == null) {
			return false;
		}

		if (jwtUtil.isExpired(token)) {
			log.info("token expired");
			removeJwtCookie(response);
			return false;
		}

		return true;
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
