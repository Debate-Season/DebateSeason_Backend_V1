package com.debateseason_backend_v1.domain.chat.controller;

import com.debateseason_backend_v1.domain.chat.presentation.controller.ChatControllerV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.debateseason_backend_v1.domain.chat.application.service.ChatServiceV1;

@ActiveProfiles("test")
@WebMvcTest(ChatControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)  // 시큐리티 필터 비활성화
class ChatControllerV1Test {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ChatServiceV1 chatServiceV1;

/*	@Test
	@DisplayName("채팅 목록 조회 성공 테스트")
	void chatListTest() throws Exception {

	}*/

/*	@Test
	@DisplayName("필수 파라미터 누락 시 실패 테스트")
	void chatListFailure() throws Exception {
		mockMvc.perform(get("/api/v1/chat/chat-list"))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}*/

}