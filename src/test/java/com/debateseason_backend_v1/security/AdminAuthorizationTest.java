package com.debateseason_backend_v1.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

/**
 * 이슈·채팅방 생성이 ADMIN 전용으로 잠겼는지 검증한다. (PRD v1.3.4 §7 1번)
 *
 * 기존 ChatRoomControllerV1Test / AdminIssueControllerV1TestEntity 는
 * 각각 본문이 통째로 주석 처리돼 있거나 addFilters=false 라서
 * 보안 필터 체인을 전혀 통과하지 않는다. 즉 인가를 검증하는 테스트가 하나도 없었다.
 * 여기서는 실제 필터 체인을 켜고 진짜 JWT 로 호출한다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthorizationTest {

	private static final int FORBIDDEN = 403;
	private static final int UNAUTHORIZED = 401;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	private String bearer(UserRole role) {
		return "Bearer " + jwtUtil.createAccessToken(1L, role);
	}

	private int statusOf(MockHttpServletRequestBuilder request) throws Exception {
		MvcResult result = mockMvc.perform(request).andReturn();
		return result.getResponse().getStatus();
	}

	@Nested
	@DisplayName("이슈 생성 POST /api/v1/issue")
	class CreateIssue {

		@Test
		@DisplayName("비로그인은 401 - 인가 이전에 인증에서 막힌다")
		void anonymous() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.post("/api/v1/issue")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"테스트 이슈\"}"));

			assertThat(status).isEqualTo(UNAUTHORIZED);
		}

		@Test
		@DisplayName("USER 는 403")
		void user() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.post("/api/v1/issue")
				.header("Authorization", bearer(UserRole.USER))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"테스트 이슈\"}"));

			assertThat(status).isEqualTo(FORBIDDEN);
		}

		@Test
		@DisplayName("ADMIN 은 인가를 통과한다")
		void admin() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.post("/api/v1/issue")
				.header("Authorization", bearer(UserRole.ADMIN))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"테스트 이슈\"}"));

			// 생성 로직의 성패는 이 테스트의 관심사가 아니다. 인가에서 막히지만 않으면 된다.
			assertThat(status).isNotIn(UNAUTHORIZED, FORBIDDEN);
		}
	}

	@Nested
	@DisplayName("채팅방 생성 POST /api/v1/room")
	class CreateChatRoom {

		@Test
		@DisplayName("비로그인은 401")
		void anonymous() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.post("/api/v1/room")
				.param("issue-id", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"테스트 방\",\"content\":\"내용\"}"));

			assertThat(status).isEqualTo(UNAUTHORIZED);
		}

		@Test
		@DisplayName("USER 는 403 - 채팅방은 이제 유저가 만들 수 없다")
		void user() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.post("/api/v1/room")
				.header("Authorization", bearer(UserRole.USER))
				.param("issue-id", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"테스트 방\",\"content\":\"내용\"}"));

			assertThat(status).isEqualTo(FORBIDDEN);
		}

		@Test
		@DisplayName("ADMIN 은 인가를 통과한다")
		void admin() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.post("/api/v1/room")
				.header("Authorization", bearer(UserRole.ADMIN))
				.param("issue-id", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"테스트 방\",\"content\":\"내용\"}"));

			assertThat(status).isNotIn(UNAUTHORIZED, FORBIDDEN);
		}
	}

	@Nested
	@DisplayName("조회 경로 회귀 방지")
	class ReadPathsStayOpen {

		@Test
		@DisplayName("GET /api/v1/issue 는 비로그인도 계속 열려 있다")
		void anonymousCanReadIssues() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.get("/api/v1/issue"));

			assertThat(status).isNotIn(UNAUTHORIZED, FORBIDDEN);
		}

		@Test
		@DisplayName("GET /api/v1/room 은 비로그인도 계속 열려 있다")
		void anonymousCanReadChatRoom() throws Exception {
			int status = statusOf(MockMvcRequestBuilders.get("/api/v1/room")
				.param("chatroom-id", "1"));

			assertThat(status).isNotIn(UNAUTHORIZED, FORBIDDEN);
		}
	}
}
