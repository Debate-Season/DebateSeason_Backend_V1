package com.debateseason_backend_v1.security.filter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.security.CustomUserDetails;
import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.service.WebServerKeys;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 웹 SSR 키가 맞는 GET 은 비로그인 IP 버킷 대신 별도 버킷을 쓴다.
 * 운영에서 Vercel 서버 IP 하나가 모든 방문자의 분당 100 을 나눠 써서 429 → 웹 500 이 났다.
 */
@DisplayName("레이트 리밋: 웹 서버 버킷")
class RateLimitWebServerBucketTest {

	private static final String KEY = "k".repeat(40);
	private static final String VERCEL_IP = "76.76.21.21";

	// 비로그인 2, 로그인 5, 웹 서버 4 — 경계를 짧게 본다
	private final RateLimitFilter filter = new RateLimitFilter(
		new SecurityPathMatcher(), new ObjectMapper(), 2, 5, new WebServerKeys("web:" + KEY), 4, 6000);

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	private int call(String method, String webKey) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/v1/issue");
		request.setRemoteAddr(VERCEL_IP);
		if (webKey != null) {
			request.addHeader(WebServerKeys.HEADER, webKey);
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());
		return response.getStatus();
	}

	@Test
	@DisplayName("키 없는 비로그인은 IP 당 한도 그대로 (3번째 429)")
	void anonymousUnchanged() throws Exception {
		assertThat(call("GET", null)).isEqualTo(200);
		assertThat(call("GET", null)).isEqualTo(200);
		assertThat(call("GET", null)).isEqualTo(429);
	}

	@Test
	@DisplayName("키가 맞는 GET 은 웹 서버 한도를 쓰고, 같은 IP 의 비로그인 버킷을 깎지 않는다")
	void webServerBucket() throws Exception {
		for (int i = 0; i < 4; i++) {
			assertThat(call("GET", KEY)).isEqualTo(200);
		}
		assertThat(call("GET", KEY)).isEqualTo(429);

		// 같은 IP 라도 키 없는 요청은 자기 버킷이 그대로 남아 있다
		assertThat(call("GET", null)).isEqualTo(200);
	}

	@Test
	@DisplayName("틀린 키는 평소처럼 IP 버킷")
	void wrongKeyFallsBack() throws Exception {
		assertThat(call("GET", "wrong")).isEqualTo(200);
		assertThat(call("GET", "wrong")).isEqualTo(200);
		assertThat(call("GET", "wrong")).isEqualTo(429);
	}

	@Test
	@DisplayName("GET 이 아니면 키가 맞아도 IP 버킷")
	void onlyGet() throws Exception {
		assertThat(call("POST", KEY)).isEqualTo(200);
		assertThat(call("POST", KEY)).isEqualTo(200);
		assertThat(call("POST", KEY)).isEqualTo(429);
	}

	@Test
	@DisplayName("로그인 사용자는 키와 무관하게 사용자 버킷")
	void authenticatedUnaffected() throws Exception {
		CustomUserDetails user = CustomUserDetails.from(7L, UserRole.USER);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		for (int i = 0; i < 5; i++) {
			assertThat(call("GET", KEY)).isEqualTo(200);
		}
		assertThat(call("GET", KEY)).isEqualTo(429);
	}
}
