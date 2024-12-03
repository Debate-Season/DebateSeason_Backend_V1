package com.debateseason_backend_v1.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.debateseason_backend_v1.security.dto.AuthResponse;
import com.debateseason_backend_v1.security.dto.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper;

	public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.objectMapper = new ObjectMapper();
		setFilterProcessesUrl("/api/v1/auth/login");
	}

	@Override
	public Authentication attemptAuthentication(
		HttpServletRequest request,
		HttpServletResponse response
	) throws AuthenticationException {

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
			username,
			password,
			null);

		return authenticationManager.authenticate(authToken);
	}

	@Override
	protected void successfulAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain chain,
		Authentication authentication
	) throws IOException {

		CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();
		String username = customUserDetails.getUsername();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		String token = jwtUtil.createJwt(username, role, 60 * 60 * 24 * 30L);
		addJwtCookie(response, token);

		AuthResponse authResponse = AuthResponse.builder()
			.status(HttpStatus.OK.value())
			.message("로그인에 성공했습니다.")
			.username(username)
			.role(role)
			.build();

		writeJsonResponse(response, HttpStatus.OK.value(), authResponse);
	}

	@Override
	protected void unsuccessfulAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException failed
	) throws IOException {

		HttpStatus status;
		String errorMessage;

		if (failed instanceof BadCredentialsException) {
			status = HttpStatus.UNAUTHORIZED;
			errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
		} else if (failed instanceof InternalAuthenticationServiceException) {
			status = HttpStatus.NOT_FOUND;
			errorMessage = "존재하지 않는 사용자입니다.";
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			errorMessage = "인증 중 오류가 발생했습니다.";
		}

		AuthResponse authResponse = AuthResponse.builder()
			.status(status.value())
			.message(errorMessage)
			.build();

		writeJsonResponse(response, status.value(), authResponse);
	}

	private void addJwtCookie(HttpServletResponse response, String token) {

		Cookie cookie = new Cookie("JWT_TOKEN", token);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24 * 30);
		response.addCookie(cookie);
	}

	private void writeJsonResponse(
		HttpServletResponse response,
		int status,
		AuthResponse authResponse
	) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), authResponse);
	}

}