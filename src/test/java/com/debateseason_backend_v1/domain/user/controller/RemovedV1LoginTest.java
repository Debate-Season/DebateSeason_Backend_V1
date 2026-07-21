package com.debateseason_backend_v1.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.debateseason_backend_v1.domain.issue.application.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.application.service.UserServiceV1;
import com.debateseason_backend_v1.domain.user.presentation.controller.UserControllerV1;

/**
 * 회귀 방지: POST /api/v1/users/login 은 소셜 ID 문자열만 받고 아무 검증 없이
 * 토큰을 발급하던 인증 우회 경로였다. 남의 소셜 ID 를 아는 사람이 그 계정으로
 * 로그인할 수 있었고, 임의 문자열로 계정을 무제한 생성할 수도 있었다.
 *
 * 정상 경로는 V2(id_token 검증)뿐이다.
 */
@ActiveProfiles("test")
@WebMvcTest(UserControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("제거된 V1 로그인")
class RemovedV1LoginTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserServiceV1 userServiceV1;

	@MockBean
	private IssueServiceV1 issueServiceV1;

	@Test
	@DisplayName("소셜 ID 만 보내도 토큰이 발급되지 않고 410 을 반환한다")
	void loginIsGoneAndIssuesNoToken() throws Exception {
		mockMvc.perform(post("/api/v1/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"identifier\":\"1323412\",\"socialType\":\"KAKAO\"}"))
			.andExpect(status().isGone())
			.andExpect(jsonPath("$.code").value("REMOVED_API"))
			// 토큰이 응답에 절대 실리면 안 된다
			.andExpect(jsonPath("$.data").doesNotExist());

		// 검증 없는 로그인 로직이 아예 호출되지 않아야 한다
		verifyNoInteractions(userServiceV1);
	}

	@Test
	@DisplayName("본문 없이 호출해도 410 이다")
	void loginIsGoneWithoutBody() throws Exception {
		mockMvc.perform(post("/api/v1/users/login"))
			.andExpect(status().isGone());

		verifyNoInteractions(userServiceV1);
	}

	@Test
	@DisplayName("검증 없는 socialLogin 메서드 자체가 코드에서 사라졌다")
	void vulnerableServiceMethodIsDeleted() {
		// 컨트롤러만 막고 서비스 로직을 남겨두면 다른 경로에서 되살아날 수 있다.
		boolean exists = Arrays.stream(UserServiceV1.class.getDeclaredMethods())
			.map(Method::getName)
			.anyMatch("socialLogin"::equals);

		assertThat(exists).isFalse();
	}
}
