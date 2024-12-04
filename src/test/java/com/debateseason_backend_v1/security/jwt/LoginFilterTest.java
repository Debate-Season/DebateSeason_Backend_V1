package com.debateseason_backend_v1.security.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.debateseason_backend_v1.security.dto.CustomUserDetails;
import com.debateseason_backend_v1.security.dto.LoginRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class LoginFilterTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private LoginFilter loginFilter;

	@Test
	@DisplayName("올바른 정보로 회원가입하면 성공한다")
	void loginSuccessWithValidCredentialsTest() throws Exception {
		// Given
		LoginRequestDTO request = new LoginRequestDTO("username", "password");
		MockHttpServletRequest httpRequest = new MockHttpServletRequest();
		httpRequest.setContent(new ObjectMapper().writeValueAsString(request).getBytes());

		Authentication expectedAuth = new UsernamePasswordAuthenticationToken("username", "password");
		when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);

		// When
		Authentication result = loginFilter.attemptAuthentication(httpRequest, new MockHttpServletResponse());

		// Then
		assertNotNull(result);
		verify(authenticationManager).authenticate(any());
	}

	@Test
	@DisplayName("올바른 인증정보로 로그인하면 성공한다")
	void successfulAuthenticationTest() throws Exception {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		Authentication auth = mock(Authentication.class);

		CustomUserDetails userDetails = mock(CustomUserDetails.class);

		when(auth.getPrincipal()).thenReturn(userDetails);
		when(userDetails.getUsername()).thenReturn("username");
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
		Collection<? extends GrantedAuthority> authorities = List.of(authority);
		doReturn(authorities).when(auth).getAuthorities();
		when(jwtUtil.createJwt(anyString(), anyString(), anyLong())).thenReturn("jwt-token");

		// when
		loginFilter.successfulAuthentication(request, response, null, auth);

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).contains("SUCCESS");
		assertThat(response.getCookies()).isNotEmpty();
		assertThat(response.getCookies()[0].getName()).isEqualTo("JWT_TOKEN");

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(response.getContentAsString());
		assertThat(jsonNode.get("data").get("username").asText()).isEqualTo("username");
		assertThat(jsonNode.get("data").get("role").asText()).isEqualTo("ROLE_USER");
	}

	@Test
	@DisplayName("잘못된 인증정보로 로그인하면 실패한다.")
	void loginFailureWithInvalidCredentialsTest() throws Exception {
		// Given
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		BadCredentialsException exception = new BadCredentialsException("Bad credentials");

		// When
		loginFilter.unsuccessfulAuthentication(request, response, exception);

		// Then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
		assertTrue(response.getContentAsString().contains("INVALID_CREDENTIALS"));
	}

	@Test
	@DisplayName("존재하지 않는 사용자로 로그인하면 실패한다.")
	void loginFailureForNonExistentUserTest() throws Exception {
		// Given
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		InternalAuthenticationServiceException exception =
			new InternalAuthenticationServiceException("User not found");

		// When
		loginFilter.unsuccessfulAuthentication(request, response, exception);

		// Then
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
		assertTrue(response.getContentAsString().contains("USER_NOT_FOUND"));
	}
}