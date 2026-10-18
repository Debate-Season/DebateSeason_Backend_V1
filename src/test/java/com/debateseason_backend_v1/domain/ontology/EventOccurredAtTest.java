package com.debateseason_backend_v1.domain.ontology;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
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

import com.debateseason_backend_v1.domain.ontology.repository.NewsEventRepository;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** 사건 최초 발생 시각(occurredAt): 같은 날 사건이 시각순으로 서고, 수정·되돌리기·옛 커서가 동작한다. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("사건 발생 시각")
class EventOccurredAtTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private NewsEventRepository newsEventRepository;

	private JsonNode call(MockHttpServletRequestBuilder request, Object body) throws Exception {
		MockHttpServletRequestBuilder r = request.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY);
		if (body != null) {
			r = r.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
		}
		MvcResult result = mockMvc.perform(r).andReturn();
		String text = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(text).isEqualTo(200);
		return objectMapper.readTree(text).path("data");
	}

	private long issue() throws Exception {
		return call(MockMvcRequestBuilders.post("/api/v1/admin/issue"),
			Map.of("proposalId", "p-" + UUID.randomUUID(), "title", "시각 이슈", "majorCategory", "사회")).path("issueId").asLong();
	}

	private List<Long> events(long issueId, List<Map<String, Object>> events) throws Exception {
		List<Long> ids = new ArrayList<>();
		call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events", issueId),
			Map.of("proposalId", "p-" + UUID.randomUUID(), "events", events)).path("eventIds").forEach(id -> ids.add(id.asLong()));
		return ids;
	}

	private JsonNode timeline(long issueId, String cursor, int size) throws Exception {
		MockHttpServletRequestBuilder r = MockMvcRequestBuilders.get("/api/v1/issue/events")
			.param("issue-id", String.valueOf(issueId)).param("size", String.valueOf(size));
		if (cursor != null) {
			r = r.param("cursor", cursor);
		}
		return call(r, null);
	}

	@Test
	@DisplayName("같은 날 사건은 발생 시각 내림차순, 날짜도 발생 시각에 맞춰진다. 날짜만 있는 사건은 그날 00:00 으로 본다")
	void ordering() throws Exception {
		long issueId = issue();
		List<Long> ids = events(issueId, List.of(
			Map.of("name", "아침", "occurredAt", "2026-10-17T08:10:00"),
			Map.of("name", "밤", "occurredAt", "2026-10-17T21:30:00", "date", "2026-10-18"),
			Map.of("name", "날짜만", "date", "2026-10-17"),
			Map.of("name", "다음 날", "occurredAt", "2026-10-18T01:00:00")));
		long morning = ids.get(0), night = ids.get(1), dateOnly = ids.get(2), nextDay = ids.get(3);

		// 발생 시각이 있으면 날짜는 그 날로 맞춘다 (밤 사건에 2026-10-18 을 줬어도 10-17)
		assertThat(newsEventRepository.findById(night).orElseThrow().getOccurredOn()).hasToString("2026-10-17");

		JsonNode items = timeline(issueId, null, 20).path("items");
		List<Long> order = new ArrayList<>();
		items.forEach(i -> order.add(i.path("eventId").asLong()));
		assertThat(order).containsExactly(nextDay, night, morning, dateOnly);
		assertThat(items.get(1).path("occurredAt").asText()).isEqualTo("2026-10-17T21:30:00");
		assertThat(items.get(3).path("occurredAt").isNull()).isTrue();

		// 커서 페이징이 시각 기준으로 이어진다
		JsonNode first = timeline(issueId, null, 2);
		JsonNode second = timeline(issueId, first.path("nextCursor").asText(), 2);
		assertThat(second.path("items").get(0).path("eventId").asLong()).isEqualTo(morning);
	}

	@Test
	@DisplayName("사건 수정으로 발생 시각을 고치고, 되돌리기로 원래 값(시각 없음)으로 돌아간다")
	void updateAndRevert() throws Exception {
		long issueId = issue();
		long event = events(issueId, List.of(Map.of("name", "수정될 사건", "date", "2026-10-18"))).get(0);

		long changeLogId = call(MockMvcRequestBuilders.patch("/api/v1/admin/event/{id}", event),
			Map.of("proposalId", "p-" + UUID.randomUUID(), "occurredAt", "2026-10-15T14:00:00")).path("changeLogId").asLong();
		var fixed = newsEventRepository.findById(event).orElseThrow();
		assertThat(fixed.getOccurredAt()).hasToString("2026-10-15T14:00");
		assertThat(fixed.getOccurredOn()).hasToString("2026-10-15");

		call(MockMvcRequestBuilders.post("/api/v1/admin/change-log/{id}/revert", changeLogId), Map.of());
		var reverted = newsEventRepository.findById(event).orElseThrow();
		assertThat(reverted.getOccurredAt()).isNull();
		assertThat(reverted.getOccurredOn()).hasToString("2026-10-18");
	}

	@Test
	@DisplayName("이미 나간 옛 형식 커서(날짜:id)도 읽는다")
	void legacyCursor() throws Exception {
		long issueId = issue();
		List<Long> ids = events(issueId, List.of(Map.of("name", "a", "date", "2026-10-10"), Map.of("name", "b", "date", "2026-10-12")));

		JsonNode page = timeline(issueId, "2026-10-12:" + ids.get(1), 20);

		assertThat(page.path("items")).hasSize(1);
		assertThat(page.path("items").get(0).path("eventId").asLong()).isEqualTo(ids.get(0));
	}
}
