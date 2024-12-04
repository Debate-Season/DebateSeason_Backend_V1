package com.debateseason_backend_v1.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResponse.ApiResponse;
import com.debateseason_backend_v1.security.dto.CustomUserDetails;
import com.debateseason_backend_v1.security.dto.LoginRequestDTO;
import com.debateseason_backend_v1.security.dto.LoginResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	bearerFormat = "JWT",
	scheme = "bearer"
)
@Tag(name = "Auth", description = "인증 관련 API")
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
		try {
			// JSON 요청 본문을 LoginRequestDTO 객체로 변환
			LoginRequestDTO loginRequestDTO = objectMapper.readValue(
				request.getInputStream(),
				LoginRequestDTO.class
			);

			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(
					loginRequestDTO.username(),  // record의 경우
					loginRequestDTO.password(),
					null
				);

			return authenticationManager.authenticate(authToken);

		} catch (IOException e) {
			throw new AuthenticationServiceException("Failed to parse authentication request body", e);
		}
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

		LoginResponseDTO loginResponseDTO = new LoginResponseDTO(username, role);

		ApiResponse<LoginResponseDTO> apiResponse = ApiResponse.success("로그인 성공", loginResponseDTO);

		writeJsonResponse(response, HttpStatus.OK.value(), apiResponse);
	}

	@Override
	protected void unsuccessfulAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException failed
	) throws IOException {

		HttpStatus status;
		ErrorCode errorCode;

		if (failed instanceof BadCredentialsException) {
			status = HttpStatus.UNAUTHORIZED;
			errorCode = ErrorCode.INVALID_CREDENTIALS;
		} else if (failed instanceof InternalAuthenticationServiceException) {
			status = HttpStatus.NOT_FOUND;
			errorCode = ErrorCode.USER_NOT_FOUND;
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			errorCode = ErrorCode.AUTHENTICATION_FAILED;
		}

		ApiResponse<Void> apiResponse = ApiResponse.error(status, errorCode);

		writeJsonResponse(response, status.value(), apiResponse);
	}

	private void addJwtCookie(HttpServletResponse response, String token) {

		Cookie cookie = new Cookie("JWT_TOKEN", token);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24 * 30);
		response.addCookie(cookie);
	}

	private void writeJsonResponse(
		HttpServletResponse response,
		int status,
		ApiResponse<?> apiResponse
	) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), apiResponse);
	}

}