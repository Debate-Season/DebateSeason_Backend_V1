package com.debateseason_backend_v1.domain.chatroom.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;

import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;

import com.debateseason_backend_v1.domain.issue.model.CommunityRecords;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;





@ActiveProfiles("test")
@WebMvcTest(ChatRoomControllerV1.class) // 컨트롤러와 관련된 애들만 빈으로 등록하고, Service, Repository같은 레이어는 빈으로 등록하지 않아 의존성을 끊습니다.
@AutoConfigureMockMvc(addFilters = false)
public class ChatRoomControllerV1Test {
	/*

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private ChatRoomServiceV1 chatRoomServiceV1;
	@MockBean
	private ChatServiceV1 chatServiceV1;

	@MockBean
	private UserDetailsService userDetailsService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("채팅방 생성하기")
	public void createChatRoom() throws JsonProcessingException, UnsupportedEncodingException {

		// Given

		// 1. admin으로부터 받은 데이터
		Long issueId = 1L;

		ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
		chatRoomDTO.setTitle("윤석열 비상 계엄");
		chatRoomDTO.setContent("12345");

		String content = new ObjectMapper().writeValueAsString(chatRoomDTO);

		// 2. 응답데이터
		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방 " + chatRoomDTO.getTitle() + "이 생성되었습니다.")
			.build();


		// When & Then

		// Response
		Mockito.when(chatRoomServiceV1.save(Mockito.any(ChatRoomDTO.class), Mockito.anyLong()))
			.thenReturn(response);

		// Request
		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(Charset.forName("UTF-8"))
					.content(content) // ChatRoomDTO
					.param("issue-id", String.valueOf(issueId)) // Issue_Id
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk())
			;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	
	@Test
	@WithMockCustomUser(role = "ROLE_USER")
	@DisplayName("채팅방 상세보기")
	public void fetchChatRoom() throws JsonProcessingException {

		// Given
		
		// 1-1. 요청 데이터
		Long chatRoomId = 1L;
		Long userId = 1L;

		// 1-2. 응답 데이터
		ChatRoomDAO chatRoomDAO = ChatRoomDAO.builder()
			.chatRoomId(chatRoomId)
			.title("트럼프 방위비 인상")
			.createdAt(LocalDateTime.now())
			.content("123456789")
			.agree(100)
			.disagree(50)
			.opinion("AGREE")
			.build();

		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방을 불러왔습니다.")
			.data(chatRoomDAO)
			.build();


		// When & Then

		// RESPONSE
		Mockito.when(chatRoomServiceV1.fetch(userId,chatRoomId)).thenReturn(response);

		// REQUEST
		try {

			mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room")
					.characterEncoding(Charset.forName("UTF-8"))
					.param("chatroom-id", String.valueOf(chatRoomId))
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	@WithMockCustomUser(role = "ROLE_USER")
	@DisplayName("채팅방 찬성/반대 투표하기")
	public void voteChatRoom() {
		
		// Given
		
		// 1-1. 요청 데이터
		Long chatRoomId = 1L;
		Long userId = 1L;
		String opinion = "AGREE";

		// 1-2. 응답 데이터
		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message(opinion + "을 투표하셨습니다.")
			.build();

		// When & Then

		// RESPONSE
		Mockito.when(chatRoomServiceV1.vote(opinion, chatRoomId, userId))
			.thenReturn(response);

		// REQUEST
		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room/vote")
					.characterEncoding(Charset.forName("UTF-8"))
					.param("opinion", opinion)
					.param("chatroom-id", String.valueOf(chatRoomId))
					.param("user-id", String.valueOf(userId))
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	// 동시성 테스트
	@Test
	@DisplayName("이슈방 접속 동시성 테스트")
	public void concurrencyTest(){
		// 각각의 사용자가 10개의 이슈방에 모두 방문을 한다.

		// Task
		Runnable task1 = new Task(1,"DC");
		Runnable task2 = new Task(2,"DC");
		Runnable task3 = new Task(3,"DC");
		Runnable task4 = new Task(4,"DC");

		Runnable task5 = new Task(5,"Fmkorea");
		Runnable task6 = new Task(6,"Fmkorea");
		Runnable task7 = new Task(7,"Fmkorea");
		Runnable task8 = new Task(8,"Fmkorea");

		Runnable task9 = new Task(9,"Nate");
		Runnable task10 = new Task(10,"Nate");
		Runnable task11 = new Task(11,"Nate");
		Runnable task12 = new Task(12,"Nate");

		// Thread
		Thread t1 = new Thread(task1);
		Thread t2 = new Thread(task2);
		Thread t3 = new Thread(task3);
		Thread t4 = new Thread(task4);

		Thread t5 = new Thread(task5);
		Thread t6 = new Thread(task6);
		Thread t7 = new Thread(task7);
		Thread t8 = new Thread(task8);

		Thread t9 = new Thread(task9);
		Thread t10 = new Thread(task10);
		Thread t11 = new Thread(task11);
		Thread t12 = new Thread(task12);


		Thread result = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("=== start ===");

					for(int i=1 ;i<11; i++){
						Map<String, Integer> map = CommunityRecords.getSortedCommunity((long)i);
						Set<String> keySet = map.keySet();

						Map<String, Integer> sortedMap = new LinkedHashMap<>();

						for (String key : keySet) {
							sortedMap.put(key, map.get(key));
						}

						//
						Set<String> keys = sortedMap.keySet();
						System.out.println("issueId "+i);
						for(String s:keys){
							System.out.print(s+" : "+sortedMap.get(s));
							System.out.println();
						}
						System.out.println();
					}

					System.out.println("=== end ===");


			}
		})
			;

		// start
		t1.start();
		t2.start();
		t3.start();
		t4.start();

		t5.start();
		t6.start();
		t7.start();
		t8.start();

		t9.start();
		t10.start();
		t11.start();
		t12.start();

		try {
			Thread.sleep(10000);
			result.start();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}




	}
}

class Task implements Runnable{

	private Long userId;
	private String community;

	Task(int userId,String community){
		this.userId=(long)userId;
		this.community=community;

	}

	@Override
	public void run() {
		UserDTO userDTO = new UserDTO();
		userDTO.setCommunity(community);
		userDTO.setId(userId);

		// 일부러 최악의 상황 설정
		for(int t=0; t<2; t++){// loop 횟수
			for(int i=1; i<11; i++){
				// 사용자가 이슈방에 접속을 한다 -> 접속 내역을 기록한다.
				CommunityRecords.record(userDTO, (long)i);
			}
		}





	}

	 */
}
