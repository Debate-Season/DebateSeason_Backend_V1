package com.debateseason_backend_v1.domain.ontology;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
import com.debateseason_backend_v1.domain.media.infrastructure.entity.MediaEntity;
import com.debateseason_backend_v1.domain.media.infrastructure.repository.MediaJpaRepository;
import com.debateseason_backend_v1.domain.ontology.repository.IssueNewsEventRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventMediaRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 사건 수정·병합·되돌리기, 타임라인 버전, 토론 주제 PREPARING 을 서비스 키로 끝까지 돌린다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("사건 타임라인·토론 주제 상태 API")
class EventTimelineApiTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MediaJpaRepository mediaJpaRepository;
	@Autowired
	private NewsEventRepository newsEventRepository;
	@Autowired
	private IssueNewsEventRepository issueNewsEventRepository;
	@Autowired
	private NewsEventMediaRepository newsEventMediaRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;

	private long issueId;

	private static String proposal() {
		return "p-" + UUID.randomUUID();
	}

	private MvcResult call(MockHttpServletRequestBuilder request, Object body) throws Exception {
		MockHttpServletRequestBuilder withKey = request.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY);
		if (body != null) {
			withKey = withKey.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
		}
		return mockMvc.perform(withKey).andReturn();
	}

	private JsonNode data(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(body).isEqualTo(200);
		return objectMapper.readTree(body).path("data");
	}

	private long media() {
		return mediaJpaRepository.save(MediaEntity.builder().title("근거 기사").type("news").build()).getId();
	}

	private List<Long> linkEvents(Map<String, Object>... events) throws Exception {
		JsonNode linked = data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events", issueId),
			Map.of("proposalId", proposal(), "events", List.of(events))));
		List<Long> ids = new ArrayList<>();
		linked.path("eventIds").forEach(id -> ids.add(id.asLong()));
		return ids;
	}

	private JsonNode versions() throws Exception {
		return data(call(MockMvcRequestBuilders.get("/api/v1/admin/issue/{id}/timeline/versions", issueId), null));
	}

	private JsonNode revert(long changeLogId) throws Exception {
		return data(call(MockMvcRequestBuilders.post("/api/v1/admin/change-log/{id}/revert", changeLogId),
			Map.of("reason", "❌", "approver", "123456789")));
	}

	@BeforeEach
	void createIssue() throws Exception {
		issueId = data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue"),
			Map.of("proposalId", proposal(), "title", "타임라인 이슈", "majorCategory", "사회"))).path("issueId").asLong();
	}

	@Nested
	@DisplayName("사건 수정")
	class UpdateEvent {

		@Test
		@DisplayName("이름·분류를 바꾸고 근거 기사를 덧붙인다. 되돌리면 원래대로, 버전은 매번 오른다")
		void updateAndRevert() throws Exception {
			long eventId = linkEvents(Map.of("name", "정원 발표", "date", "2026-10-30", "major", "사회")).get(0);
			long mediaId = media();

			JsonNode updated = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", eventId),
				Map.of("proposalId", proposal(), "name", "정원 2천 명 확정", "middle", "의료", "addMediaIds", List.of(mediaId))));
			long changeLogId = updated.path("changeLogId").asLong();

			assertThat(newsEventRepository.findById(eventId).orElseThrow().getName()).isEqualTo("정원 2천 명 확정");
			assertThat(newsEventMediaRepository.findByEventId(eventId)).hasSize(1);
			JsonNode afterUpdate = versions();
			assertThat(afterUpdate.get(0).path("version").asInt()).isEqualTo(2);
			assertThat(afterUpdate.get(0).path("changeLogId").asLong()).isEqualTo(changeLogId);
			assertThat(afterUpdate.get(0).path("snapshot").get(0).path("name").asText()).isEqualTo("정원 2천 명 확정");

			JsonNode reverted = revert(changeLogId);
			assertThat(reverted.path("changed").asBoolean()).isTrue();
			assertThat(newsEventRepository.findById(eventId).orElseThrow().getName()).isEqualTo("정원 발표");
			assertThat(newsEventRepository.findById(eventId).orElseThrow().getMiddleCategory()).isNull();
			assertThat(newsEventMediaRepository.findByEventId(eventId)).isEmpty();
			assertThat(versions().get(0).path("version").asInt()).isEqualTo(3);

			// 같은 되돌리기 재시도
			assertThat(revert(changeLogId).path("changed").asBoolean()).isFalse();
			assertThat(versions().get(0).path("version").asInt()).isEqualTo(3);
		}

		@Test
		@DisplayName("그 뒤 같은 사건이 또 바뀌었으면 앞선 변경은 되돌리지 않는다 (409)")
		void revertConflict() throws Exception {
			long eventId = linkEvents(Map.of("name", "처음")).get(0);
			long first = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", eventId),
				Map.of("proposalId", proposal(), "name", "두 번째"))).path("changeLogId").asLong();
			data(call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", eventId),
				Map.of("proposalId", proposal(), "name", "세 번째")));

			MvcResult result = call(MockMvcRequestBuilders.post("/api/v1/admin/change-log/{id}/revert", first), Map.of());

			assertThat(result.getResponse().getStatus()).isEqualTo(409);
			assertThat(newsEventRepository.findById(eventId).orElseThrow().getName()).isEqualTo("세 번째");
		}

		@Test
		@DisplayName("같은 proposalId 재시도는 처음 changeLogId, 바뀔 게 없으면 changeLogId=null")
		void idempotent() throws Exception {
			long eventId = linkEvents(Map.of("name", "재시도 사건")).get(0);
			Map<String, Object> body = Map.of("proposalId", proposal(), "name", "새 이름");

			long first = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", eventId), body)).path("changeLogId").asLong();
			JsonNode again = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", eventId), body));
			assertThat(again.path("changeLogId").asLong()).isEqualTo(first);
			assertThat(again.path("changed").asBoolean()).isFalse();

			JsonNode noop = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", eventId),
				Map.of("proposalId", proposal(), "name", "새 이름")));
			assertThat(noop.path("changeLogId").isNull()).isTrue();
		}
	}

	@Nested
	@DisplayName("사건 병합")
	class MergeEvents {

		@Test
		@DisplayName("drop 의 근거를 keep 에 복사하고 drop 을 이 이슈에서 뗀다. 되돌리면 다시 붙는다")
		void mergeAndRevert() throws Exception {
			List<Long> ids = linkEvents(Map.of("name", "남길 사건", "date", "2026-10-01"), Map.of("name", "흡수될 사건", "date", "2026-10-02"));
			long keep = ids.get(0);
			long drop = ids.get(1);
			long mediaId = media();
			data(call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", drop),
				Map.of("proposalId", proposal(), "addMediaIds", List.of(mediaId))));

			long changeLogId = data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events/merge", issueId),
				Map.of("proposalId", proposal(), "keepEventId", keep, "dropEventId", drop))).path("changeLogId").asLong();

			assertThat(newsEventRepository.findById(drop).orElseThrow().getMergedInto()).isEqualTo(keep);
			assertThat(issueNewsEventRepository.findByIssueIdAndEventId(issueId, drop)).isEmpty();
			assertThat(newsEventMediaRepository.findByEventId(keep)).extracting(m -> m.getMediaId()).containsExactly(mediaId);
			assertThat(newsEventMediaRepository.findByEventId(drop)).hasSize(1);
			assertThat(versions().get(0).path("snapshot")).hasSize(1);

			revert(changeLogId);

			assertThat(newsEventRepository.findById(drop).orElseThrow().getMergedInto()).isNull();
			assertThat(issueNewsEventRepository.findByIssueIdAndEventId(issueId, drop)).isPresent();
			assertThat(newsEventMediaRepository.findByEventId(keep)).isEmpty();
			JsonNode snapshot = versions().get(0).path("snapshot");
			assertThat(snapshot).hasSize(2);
			assertThat(snapshot.get(0).path("id").asLong()).isEqualTo(keep);
		}

		@Test
		@DisplayName("같은 사건끼리, 이 이슈에 없는 사건은 병합할 수 없다 (409)")
		void invalidMerge() throws Exception {
			long only = linkEvents(Map.of("name", "하나뿐")).get(0);

			MvcResult same = call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events/merge", issueId),
				Map.of("proposalId", proposal(), "keepEventId", only, "dropEventId", only));
			assertThat(same.getResponse().getStatus()).isEqualTo(409);
		}
	}

	@Test
	@DisplayName("사건 추가·해제도 버전을 올리고, 추가 같은 지원하지 않는 이력은 되돌리기 409")
	void linkUnlinkVersionsAndUnsupportedRevert() throws Exception {
		List<Long> ids = linkEvents(Map.of("name", "B", "date", "2026-10-05"), Map.of("name", "A", "date", "2026-10-01"));
		JsonNode v1 = versions().get(0);
		assertThat(v1.path("version").asInt()).isEqualTo(1);
		// 날짜순
		assertThat(v1.path("snapshot").get(0).path("name").asText()).isEqualTo("A");

		data(call(MockMvcRequestBuilders.delete("/api/v1/admin/issue/{id}/events", issueId), Map.of("eventIds", List.of(ids.get(0)))));
		assertThat(versions().get(0).path("version").asInt()).isEqualTo(2);
		assertThat(versions().get(0).path("snapshot")).hasSize(1);

		long linkLogId = v1.path("changeLogId").asLong();
		assertThat(call(MockMvcRequestBuilders.post("/api/v1/admin/change-log/{id}/revert", linkLogId), Map.of())
			.getResponse().getStatus()).isEqualTo(409);
	}

	@Nested
	@DisplayName("토론 주제 PREPARING")
	class Preparing {

		private long createTopic(Object status) throws Exception {
			Map<String, Object> body = new java.util.HashMap<>(Map.of("proposalId", proposal(), "title", "준비 중 주제"));
			if (status != null) {
				body.put("status", status);
			}
			return data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/debate-topic", issueId), body))
				.path("chatRoomId").asLong();
		}

		@Test
		@DisplayName("PREPARING 으로 만들면 목록에 없고, OPEN 으로 바꾸면 나타나고, CLOSED(완료)로도 바꿀 수 있다")
		void lifecycle() throws Exception {
			long topic = createTopic("PREPARING");
			assertThat(chatRoomRepository.findById(topic).orElseThrow().getStatus()).isEqualTo(ChatRoomStatus.PREPARING);
			assertThat(chatRoomRepository.findThreadRoomIdsByIssueId(issueId)).doesNotContain(topic);

			JsonNode opened = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/room/{id}/status", topic), Map.of("status", "OPEN")));
			assertThat(opened.path("fromStatus").asText()).isEqualTo("PREPARING");
			assertThat(chatRoomRepository.findThreadRoomIdsByIssueId(issueId)).contains(topic);

			JsonNode closed = data(call(MockMvcRequestBuilders.patch("/api/v1/admin/room/{id}/status", topic), Map.of("status", "CLOSED")));
			assertThat(closed.path("fromStatus").asText()).isEqualTo("OPEN");
			assertThat(chatRoomRepository.findThreadRoomIdsByIssueId(issueId)).contains(topic);
		}

		@Test
		@DisplayName("status 를 비우면 OPEN, 처음부터 CLOSED 는 400, DELETED 로 바꾸기는 409")
		void initialStatusRules() throws Exception {
			long topic = createTopic(null);
			assertThat(chatRoomRepository.findById(topic).orElseThrow().getStatus()).isEqualTo(ChatRoomStatus.OPEN);

			MvcResult closedAtStart = call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/debate-topic", issueId),
				Map.of("proposalId", proposal(), "title", "x", "status", "CLOSED"));
			assertThat(closedAtStart.getResponse().getStatus()).isEqualTo(400);

			MvcResult deleted = call(MockMvcRequestBuilders.patch("/api/v1/admin/room/{id}/status", topic), Map.of("status", "DELETED"));
			assertThat(deleted.getResponse().getStatus()).isEqualTo(409);
		}
	}

	@Test
	@DisplayName("타임라인 버전 GET 은 키 없이 401, 다른 ADMIN GET 은 서비스 키로도 열리지 않는다")
	void versionsAuth() throws Exception {
		assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/issue/{id}/timeline/versions", issueId))
			.andReturn().getResponse().getStatus()).isEqualTo(401);
		assertThat(call(MockMvcRequestBuilders.get("/api/v1/admin/issue/{id}/status", issueId), null)
			.getResponse().getStatus()).isEqualTo(401);
	}
}
