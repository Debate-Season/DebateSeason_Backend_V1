package com.debateseason_backend_v1.domain.chatroom.controller;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GET /api/v1/room 의 상위 관계 필드. 웹 SSR 은 토큰 없이 부르고, 스레드 id 하나로 이슈·컨테이너를 찾는다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("채팅방 단건 응답의 상위 관계 필드")
class ChatRoomParentFieldsTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private IssueJpaRepository issueJpaRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;

	private IssueEntity issue;
	private ChatRoom container;

	@BeforeEach
	void setUp() {
		issue = issueJpaRepository.save(IssueEntity.builder().title("의대 정원 확대").majorCategory("사회").build());
		container = chatRoomRepository.save(ChatRoom.builder()
			.issueEntity(issue).title(issue.getTitle()).roomType(ChatRoomType.CONTAINER).build());
	}

	private ChatRoom room(ChatRoomType type, Long containerId, ChatRoomStatus status) {
		return chatRoomRepository.save(ChatRoom.builder()
			.issueEntity(issue).title("주제").content("#태그").roomType(type).containerRoomId(containerId).status(status).build());
	}

	private MvcResult fetchAnonymously(Long chatRoomId) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/room").param("chatroom-id", String.valueOf(chatRoomId)))
			.andReturn();
	}

	private JsonNode data(Long chatRoomId) throws Exception {
		MvcResult result = fetchAnonymously(chatRoomId);
		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data");
	}

	@Test
	@DisplayName("스레드: 익명 200, 이슈·컨테이너·상태·수정시각을 싣는다")
	void thread() throws Exception {
		ChatRoom thread = room(ChatRoomType.THREAD, container.getId(), ChatRoomStatus.OPEN);

		JsonNode data = data(thread.getId());

		assertThat(data.path("issueId").asLong()).isEqualTo(issue.getId());
		assertThat(data.path("issueTitle").asText()).isEqualTo("의대 정원 확대");
		assertThat(data.path("roomType").asText()).isEqualTo("THREAD");
		assertThat(data.path("containerRoomId").asLong()).isEqualTo(container.getId());
		assertThat(data.path("status").asText()).isEqualTo("OPEN");
		assertThat(data.path("updatedAt").asText()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
		// 기존 필드는 그대로
		assertThat(data.path("chatRoomId").asLong()).isEqualTo(thread.getId());
		assertThat(data.path("title").asText()).isEqualTo("주제");
	}

	@Test
	@DisplayName("컨테이너: roomType=CONTAINER, containerRoomId=null — 웹이 스레드가 아닌 id 를 걸러 낼 수 있다")
	void container() throws Exception {
		JsonNode data = data(container.getId());

		assertThat(data.path("roomType").asText()).isEqualTo("CONTAINER");
		assertThat(data.path("containerRoomId").isNull()).isTrue();
	}

	@Test
	@DisplayName("레거시 방(room_type NULL)은 THREAD 로 내려간다")
	void legacyIsThread() throws Exception {
		JsonNode data = data(room(null, null, ChatRoomStatus.OPEN).getId());

		assertThat(data.path("roomType").asText()).isEqualTo("THREAD");
	}

	@Test
	@DisplayName("HIDDEN 방도 200 으로 status 를 알린다 — 404 처리는 웹이 한다")
	void hiddenReportsStatus() throws Exception {
		JsonNode data = data(room(ChatRoomType.THREAD, container.getId(), ChatRoomStatus.HIDDEN).getId());

		assertThat(data.path("status").asText()).isEqualTo("HIDDEN");
	}

	@Test
	@DisplayName("없는 id 는 404 (기존 동작)")
	void missing() throws Exception {
		assertThat(fetchAnonymously(987654321L).getResponse().getStatus()).isEqualTo(404);
	}
}
