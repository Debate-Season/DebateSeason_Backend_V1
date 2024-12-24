package com.debateseason_backend_v1.domain.chatroom.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.dto.ResponseDTO;
import com.debateseason_backend_v1.domain.chatroom.model.response.ChatRoomResponse;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@WebMvcTest(ChatRoomControllerV1.class) // 컨트롤러와 관련된 애들만 빈으로 등록하고, Service, Repository같은 레이어는 빈으로 등록하지 않아 의존성을 끊습니다.
@AutoConfigureMockMvc(addFilters = false)
public class ChatRoomControllerV1Test {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private ChatRoomServiceV1 chatRoomServiceV1;
	@MockBean
	private ChatServiceV1 chatServiceV1;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("채팅방 생성 200")
	public void createChatRoom() throws JsonProcessingException, UnsupportedEncodingException {

		// Given

		// 1. REQUEST
		Long issueId = 1L;

		ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
		chatRoomDTO.setTitle("윤석열 비상계엄 발동");
		chatRoomDTO.setContent("12345");

		String content = new ObjectMapper().writeValueAsString(chatRoomDTO);

		// 2. RESPONSE
		ChatRoomResponse responseMap = new ChatRoomResponse();
		responseMap.setMessage("Successfully make ChatRoom!");

		Mockito.when(chatRoomServiceV1.save(Mockito.any(ChatRoomDTO.class), Mockito.anyLong()))
			.thenReturn(ResponseEntity.ok("Successfully make ChatRoom!"));

		// When & Then
		// Exception으로 하면 세부적인 오류를 파악할 수 없음. 그래서 try-catch

		// 2. API 요청 받기
		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(Charset.forName("UTF-8"))
					.content(content) // ChatRoomDTO
					.param("issue-id", String.valueOf(issueId)) // Issue_Id
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Successfully make ChatRoom!")) // 응답 본문 검증

			;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	@DisplayName("존재하지 않는 이슈방에 채팅방 생성 -> Throw")
	public void createChatRoomWithNot_Exist_IssueRoom() throws JsonProcessingException {

		// Given

		// REQUEST
		Long issueId = -999999999L; // 존재하지 않는 이슈방 번호

		ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
		chatRoomDTO.setTitle("대왕 고래 시추 준비중");
		chatRoomDTO.setContent("12345");

		String content = new ObjectMapper().writeValueAsString(chatRoomDTO);

		// RESPONSE
		ChatRoomResponse responseMap = new ChatRoomResponse();
		responseMap.setMessage("Successfully make ChatRoom!");

		Mockito.when(chatRoomServiceV1.save(Mockito.any(ChatRoomDTO.class), Mockito.anyLong()))
			.thenThrow(new RuntimeException("There is no " + issueId))
		;

		// 1. API 요청 받기
		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(Charset.forName("UTF-8"))
					.content(content) // ChatRoomDTO
					.param("issue-id", String.valueOf(issueId)) // Issue_Id
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Successfully make ChatRoom!")) // 응답 본문 검증

			;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	@DisplayName("채팅방 상세보기")
	public void fetchChatRoom() throws JsonProcessingException {

		// Given
		Long chatRoomId = 1L;

		//
		ChatRoomDAO chatRoomDAO = ChatRoomDAO.builder()
			.id(chatRoomId)
			.title("트럼프 방위비 인상")
			.content("123456789")
			.agree(100)
			.disagree(50)
			.build();

		ResponseDTO responseDTO = ResponseDTO.builder()
			.chatRoomDAO(chatRoomDAO)
			.build();

		String response = objectMapper.writeValueAsString(responseDTO);

		Mockito.when(chatRoomServiceV1.fetch(chatRoomId)).thenReturn(ResponseEntity.ok(response));

		// When & Then
		try {

			mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room")
					.characterEncoding(Charset.forName("UTF-8"))
					.param("chatroom-id", String.valueOf(chatRoomId))
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(response));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("존재하지 않는 채팅방을 조회한 경우 -> Throw")
	public void fetchChatRoomWithNot_Exist_ChatRoom() throws JsonProcessingException {

		// Given
		Long chatRoomId = -999999999L;

		//
		ChatRoomDAO chatRoomDAO = ChatRoomDAO.builder()
			.id(chatRoomId)
			.title("트럼프 방위비 인상")
			.content("123456789")
			.agree(100)
			.disagree(50)
			.build();

		ResponseDTO responseDTO = ResponseDTO.builder()
			.chatRoomDAO(chatRoomDAO)
			.build();

		String response = objectMapper.writeValueAsString(responseDTO);

		Mockito.when(chatRoomServiceV1.fetch(chatRoomId))
			.thenThrow(new RuntimeException("There is no ChatRoom: " + chatRoomId));

		// When & Then
		try {

			mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room")
					.characterEncoding(Charset.forName("UTF-8"))
					.param("chatroom-id", String.valueOf(chatRoomId))
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(response));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("채팅방 찬성/반대 투표하기")
	public void voteChatRoom() {
		Long chatRoomId = 1L;
		Long userId = 1L;
		String opinion = "AGREE";

		Mockito.when(chatRoomServiceV1.vote(opinion, chatRoomId, userId))
			.thenReturn(ResponseEntity.ok("Vote Successfully"));

		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/vote")
					.characterEncoding(Charset.forName("UTF-8"))
					.param("opinion", opinion)
					.param("chatroom-id", String.valueOf(chatRoomId))
					.param("user-id", String.valueOf(userId))
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Vote Successfully"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
