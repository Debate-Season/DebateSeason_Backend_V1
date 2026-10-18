package com.debateseason_backend_v1.domain.ontology;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.ontology.repository.IssueNewsEventRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 온톨로지 승인 반영(✅)·롤백(❌)을 실제 필터 체인 + 서비스 키로 끝까지 돌린다.
 * 롤백 뒤에 앱 조회 경로에서 정말 사라지는지까지 본다 — 상태만 바뀌고 노출은 그대로인 게 가장 흔한 실패다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("온톨로지 반영·롤백 API")
class OntologyAdminApiTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private IssueJpaRepository issueJpaRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;
	@Autowired
	private NewsEventRepository newsEventRepository;
	@Autowired
	private IssueNewsEventRepository issueNewsEventRepository;

	private static String proposal() {
		return "p-" + UUID.randomUUID();
	}

	private MvcResult call(MockHttpServletRequestBuilder request, Object body) throws Exception {
		return mockMvc.perform(request
				.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andReturn();
	}

	private JsonNode data(MvcResult result) throws Exception {
		assertThat(result.getResponse().getStatus())
			.as(result.getResponse().getContentAsString())
			.isEqualTo(200);
		return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data");
	}

	private JsonNode createIssue(String proposalId, String title) throws Exception {
		return data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue"),
			java.util.Map.of("proposalId", proposalId, "title", title, "majorCategory", "사회",
				"reason", "신규 사건 묶음", "approver", "123456789")));
	}

	@Nested
	@DisplayName("이슈 신설")
	class CreateIssue {

		@Test
		@DisplayName("PUBLISHED 이슈와 컨테이너 방을 함께 만든다")
		void createsIssueWithContainer() throws Exception {
			JsonNode created = createIssue(proposal(), "의대 정원 확대");

			long issueId = created.path("issueId").asLong();
			assertThat(created.path("created").asBoolean()).isTrue();
			assertThat(issueJpaRepository.findById(issueId).orElseThrow().getStatus()).isEqualTo(IssueStatus.PUBLISHED);

			ChatRoom container = chatRoomRepository.findById(created.path("containerRoomId").asLong()).orElseThrow();
			assertThat(container.getRoomType()).isEqualTo(ChatRoomType.CONTAINER);
			assertThat(container.getTitle()).isEqualTo("의대 정원 확대");
			assertThat(chatRoomRepository.findFirstByIssueEntity_IdAndRoomType(issueId, ChatRoomType.CONTAINER)).isPresent();
		}

		@Test
		@DisplayName("같은 proposalId 재시도는 새로 만들지 않고 처음 결과를 돌려준다")
		void idempotentByProposalId() throws Exception {
			String proposalId = proposal();
			JsonNode first = createIssue(proposalId, "재시도 이슈");
			long before = issueJpaRepository.count();

			JsonNode second = createIssue(proposalId, "재시도 이슈");

			assertThat(second.path("created").asBoolean()).isFalse();
			assertThat(second.path("issueId").asLong()).isEqualTo(first.path("issueId").asLong());
			assertThat(second.path("containerRoomId").asLong()).isEqualTo(first.path("containerRoomId").asLong());
			assertThat(issueJpaRepository.count()).isEqualTo(before);
		}

		@Test
		@DisplayName("롤백(ARCHIVED)하면 상세는 404, 이슈맵에서도 빠진다. 다시 PUBLISHED 로 되살릴 수 있다")
		void rollbackHidesIssue() throws Exception {
			long issueId = createIssue(proposal(), "롤백될 이슈").path("issueId").asLong();

			data(call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId),
				java.util.Map.of("status", "ARCHIVED", "reason", "❌ 롤백", "approver", "123456789")));

			int detail = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/issue").param("issue-id", String.valueOf(issueId)))
				.andReturn().getResponse().getStatus();
			assertThat(detail).isEqualTo(404);
			assertThat(issueJpaRepository.findTop6Issues()).doesNotContain(issueId);
			assertThat(issueJpaRepository.findAllByStatusNotOrderByCreatedAtDescIdDesc(IssueStatus.ARCHIVED))
				.extracting(issue -> issue.getId()).doesNotContain(issueId);

			data(call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId),
				java.util.Map.of("status", "PUBLISHED", "reason", "✅ 재승인", "approver", "123456789")));
			assertThat(issueJpaRepository.findTop6Issues()).contains(issueId);
		}
	}

	@Test
	@DisplayName("이슈명 변경: previousTitle 을 돌려주고 컨테이너 방 이름도 따라 바뀐다. previousTitle 로 다시 부르면 롤백")
	void renameAndRollback() throws Exception {
		JsonNode created = createIssue(proposal(), "옛 이름");
		long issueId = created.path("issueId").asLong();

		JsonNode renamed = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}", issueId),
			java.util.Map.of("title", "새 이름", "approver", "123456789")));
		assertThat(renamed.path("previousTitle").asText()).isEqualTo("옛 이름");
		assertThat(chatRoomRepository.findById(created.path("containerRoomId").asLong()).orElseThrow().getTitle())
			.isEqualTo("새 이름");

		JsonNode rolledBack = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}", issueId),
			java.util.Map.of("title", renamed.path("previousTitle").asText(), "approver", "123456789")));
		assertThat(rolledBack.path("changed").asBoolean()).isTrue();
		assertThat(issueJpaRepository.findById(issueId).orElseThrow().getTitle()).isEqualTo("옛 이름");

		JsonNode again = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}", issueId),
			java.util.Map.of("title", "옛 이름")));
		assertThat(again.path("changed").asBoolean()).isFalse();
	}

	@Nested
	@DisplayName("토론 주제")
	class DebateTopic {

		@Test
		@DisplayName("컨테이너 아래 OPEN 스레드로 만들고, HIDDEN 롤백 시 이슈의 토론 목록에서 빠진다")
		void createAndHide() throws Exception {
			JsonNode issue = createIssue(proposal(), "토론 주제 이슈");
			long issueId = issue.path("issueId").asLong();
			String proposalId = proposal();
			java.util.Map<String, String> body = java.util.Map.of("proposalId", proposalId,
				"title", "2027학년도부터 해야 하나", "content", "#의대증원", "approver", "123456789");

			JsonNode topic = data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/debate-topic", issueId), body));
			long threadId = topic.path("chatRoomId").asLong();

			ChatRoom thread = chatRoomRepository.findById(threadId).orElseThrow();
			assertThat(thread.getRoomType()).isEqualTo(ChatRoomType.THREAD);
			assertThat(thread.getContainerRoomId()).isEqualTo(issue.path("containerRoomId").asLong());
			assertThat(thread.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
			assertThat(chatRoomRepository.findThreadRoomIdsByIssueId(issueId)).containsExactly(threadId);

			// 재시도
			JsonNode retry = data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/debate-topic", issueId), body));
			assertThat(retry.path("chatRoomId").asLong()).isEqualTo(threadId);
			assertThat(retry.path("created").asBoolean()).isFalse();

			data(call(MockMvcRequestBuilders.patch("/api/v1/admin/room/{id}/status", threadId),
				java.util.Map.of("status", "HIDDEN", "approver", "123456789")));
			assertThat(chatRoomRepository.findThreadRoomIdsByIssueId(issueId)).isEmpty();
			assertThat(chatRoomRepository.findTop3ChatRoomIdsByIssueId(issueId)).isEmpty();
		}

		@Test
		@DisplayName("컨테이너 방은 숨길 수 없다 (409) — 숨기면 이슈 채팅 전체가 사라진다")
		void containerCannotBeHidden() throws Exception {
			long containerId = createIssue(proposal(), "컨테이너 보호").path("containerRoomId").asLong();

			MvcResult result = call(MockMvcRequestBuilders.patch("/api/v1/admin/room/{id}/status", containerId),
				java.util.Map.of("status", "HIDDEN"));

			assertThat(result.getResponse().getStatus()).isEqualTo(409);
		}
	}

	@Nested
	@DisplayName("사건")
	class Events {

		@Test
		@DisplayName("사건을 만들어 연결하고, 롤백은 연결만 끊는다 (사건 행은 남는다)")
		void linkAndUnlink() throws Exception {
			long issueId = createIssue(proposal(), "사건 이슈").path("issueId").asLong();

			JsonNode linked = data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events", issueId),
				java.util.Map.of("proposalId", proposal(), "approver", "123456789", "events", java.util.List.of(
					java.util.Map.of("name", "정원 발표", "date", "2026-10-30", "major", "사회", "middle", "의료"),
					java.util.Map.of("name", "전공의 성명")))));

			java.util.List<Long> eventIds = new java.util.ArrayList<>();
			linked.path("eventIds").forEach(id -> eventIds.add(id.asLong()));
			assertThat(eventIds).hasSize(2);
			assertThat(issueNewsEventRepository.findByIssueIdAndEventIdIn(issueId, eventIds)).hasSize(2);

			JsonNode unlinked = data(call(MockMvcRequestBuilders.delete("/api/v1/admin/issue/{id}/events", issueId),
				java.util.Map.of("eventIds", eventIds, "approver", "123456789")));
			assertThat(unlinked.path("unlinkedEventIds")).hasSize(2);
			assertThat(issueNewsEventRepository.findByIssueIdAndEventIdIn(issueId, eventIds)).isEmpty();
			assertThat(newsEventRepository.findAllById(eventIds)).hasSize(2);

			// 롤백 재시도는 빈 결과로 200
			JsonNode again = data(call(MockMvcRequestBuilders.delete("/api/v1/admin/issue/{id}/events", issueId),
				java.util.Map.of("eventIds", eventIds)));
			assertThat(again.path("unlinkedEventIds")).isEmpty();
		}

		@Test
		@DisplayName("없는 media id 가 섞이면 404 이고 아무것도 저장하지 않는다")
		void unknownMediaRejected() throws Exception {
			long issueId = createIssue(proposal(), "근거 없는 사건").path("issueId").asLong();
			long eventsBefore = newsEventRepository.count();

			MvcResult result = call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events", issueId),
				java.util.Map.of("proposalId", proposal(), "events", java.util.List.of(
					java.util.Map.of("name", "근거 없음", "mediaIds", java.util.List.of(987654321L)))));

			assertThat(result.getResponse().getStatus()).isEqualTo(404);
			assertThat(newsEventRepository.count()).isEqualTo(eventsBefore);
		}
	}

	@Test
	@DisplayName("서비스 키는 반영 API 밖의 ADMIN 경로를 열지 못한다")
	void serviceKeyScope() throws Exception {
		MvcResult wiki = call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/publish"), java.util.Map.of("revisionId", 1));
		MvcResult legacyCreate = call(MockMvcRequestBuilders.post("/api/v1/issue"), java.util.Map.of("title", "x"));

		assertThat(wiki.getResponse().getStatus()).isEqualTo(401);
		assertThat(legacyCreate.getResponse().getStatus()).isEqualTo(401);
	}
}
