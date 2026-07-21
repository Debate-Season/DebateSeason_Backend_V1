package com.debateseason_backend_v1.domain.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.debateseason_backend_v1.domain.chat.application.service.ReportService;
import com.debateseason_backend_v1.domain.chat.presentation.controller.ReportController;
import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.security.CustomUserDetails;

@ActiveProfiles("test")
@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)  // 시큐리티 필터 비활성화 (principal 은 아래에서 직접 주입)
class ReportControllerTest {

	private static final Long AUTHENTICATED_USER_ID = 7L;
	private static final Long ATTACKER_SUPPLIED_USER_ID = 999L;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReportService reportService;

	/**
	 * 회귀 방지: 이전에는 신고자 userId 가 어노테이션 없는 컨트롤러 파라미터라
	 * 요청 파라미터(?userId=)로 바인딩됐다. 즉 클라이언트가 남의 이름으로 신고할 수 있었다.
	 * 이제는 요청 파라미터를 실어 보내도 인증된 principal 의 userId 만 쓰여야 한다.
	 */
	@Test
	@DisplayName("요청 파라미터로 userId 를 보내도 신고자는 인증된 사용자여야 한다")
	void reporterIdCannotBeSpoofedByRequestParam() throws Exception {
		authenticateAs(AUTHENTICATED_USER_ID);

		mockMvc.perform(post("/api/v1/chat/messages/{messageId}/report", 1L)
			.param("userId", String.valueOf(ATTACKER_SUPPLIED_USER_ID))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"reasonType\":[\"ABUSE\"],\"reasonDetail\":\"욕설\"}"));

		verify(reportService).createChatReport(
			eq(1L),
			eq(AUTHENTICATED_USER_ID),   // 999L 이 아니어야 한다
			any(),
			any()
		);
	}

	private void authenticateAs(Long userId) {
		CustomUserDetails principal = CustomUserDetails.from(userId, UserRole.USER);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
		);
	}
}
