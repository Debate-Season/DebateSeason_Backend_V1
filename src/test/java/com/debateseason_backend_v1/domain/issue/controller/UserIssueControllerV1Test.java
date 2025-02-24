package com.debateseason_backend_v1.domain.issue.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.controller.WithMockCustomUser;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.chatroom.model.response.ChatRoomResponse;
import com.debateseason_backend_v1.domain.issue.dto.IssueDAO;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.service.UserIssueServiceV1;

@ActiveProfiles("test")
@WebMvcTest(UserIssueControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserIssueControllerV1Test {

	@Autowired
	private MockMvc mockMvc;

	// @MockBean을 쓸 경우 @WebMvcTest에서 사용된 Controller에 주입된 환경과 동일하게 설정해야지, 하나라도 빼먹으면 안됨.
	@MockBean
	private IssueServiceV1 issueServiceV1;
	@MockBean
	private UserIssueServiceV1 userIssueServiceV1;
	
	@Test
	@WithMockCustomUser(role = "ROLE_USER")
	@DisplayName("이슈방 1건 상세보기")
	public void getIssue(){
/*
		// given

		// 이슈방 id가 주어짐
		Long issueId = 1L;

		// 사용자 id가 주어짐.
		Long userId = 1L;

		// 커뮤니티 내림차순으로 정렬하기 위한 자료구조( LinkedHashMap으로 순서를 고정한다 )
		Map<String, Integer> sortedMap = new LinkedHashMap<>();
		sortedMap.put("community/icons/dcinside.png",3);
		sortedMap.put("community/icons/fmkorea.png",2);
		sortedMap.put("community/icons/theqoo.png",1);


		// 채팅방DAO 및 이를 담는 chatRoomMap
		ChatRoomResponse chatRoomDAO = ChatRoomResponse.builder()
			.chatRoomId(1L)
			.title("미국 관세 25%부여는 합당한가")
			.content("11111")
			//.issue(c.getIssue())
			.createdAt(LocalDateTime.now())
			.agree(10L)
			.disagree(5L)
			.build();

		List<ChatRoomResponse> chatRoomMap = new ArrayList<>();
		chatRoomMap.add(chatRoomDAO);

		// 이슈방 DAO
		// 제목 + 커뮤니티 내림차순으로 정렬한 것 + 채팅방DAO를 담은 chatRoomMap
		IssueDAO issueDAO = IssueDAO.builder()
			.title("미국 관세 전쟁")
			.map(sortedMap)
			//.chatRoomMap(chatRoomMap)
			.build();

		// Reponse값
		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueId + "조회")
			.data(issueDAO)
			.build();

		// When & Then

		// RESPONSE
		Mockito.when(issueServiceV1.fetch(issueId,userId))
			.thenReturn(response);

		// REQUEST
		try {

			mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/issue")
					.characterEncoding(Charset.forName("UTF-8"))
					.param("issue-id", String.valueOf(issueId))
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

 */

	}
}
