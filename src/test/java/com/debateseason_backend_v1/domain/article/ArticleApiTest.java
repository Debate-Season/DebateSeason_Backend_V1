package com.debateseason_backend_v1.domain.article;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleRepository;
import com.debateseason_backend_v1.domain.media.infrastructure.entity.MediaEntity;
import com.debateseason_backend_v1.domain.media.infrastructure.repository.MediaJpaRepository;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 토론철 아티클: 적재(REVIEW) → 게시 → 공개 조회 → 회수, 사건 타임라인, 사이트맵.
 * 데이터 레포가 서비스 키로 부르는 실제 경로 그대로 돌린다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("토론철 아티클 API")
class ArticleApiTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MediaJpaRepository mediaJpaRepository;
	@Autowired
	private ToronchulArticleRepository articleRepository;

	private long issueId;
	private long mediaId;

	private static String key() {
		return "test/toronchul/" + UUID.randomUUID();
	}

	private MvcResult admin(MockHttpServletRequestBuilder request, Object body) throws Exception {
		MockHttpServletRequestBuilder r = request.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY);
		if (body != null) {
			r = r.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
		}
		return mockMvc.perform(r).andReturn();
	}

	private MvcResult anonymous(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request).andReturn();
	}

	private JsonNode data(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(body).isEqualTo(200);
		return objectMapper.readTree(body).path("data");
	}

	private Map<String, Object> article(String articleKey, String edition, List<Long> events, List<Long> issues) {
		Map<String, Object> body = new HashMap<>();
		body.put("articleKey", articleKey);
		body.put("edition", edition);
		body.put("headline", "코스피, 8,000선 돌파 뒤 하루 만에 급락");
		body.put("summary", "코스피가 8,000선을 넘은 다음 날 4% 넘게 떨어졌다. 반도체 차익 실현이 겹쳤다.");
		body.put("body", List.of(
			Map.of("text", "첫 문단이다.", "sources", List.of(mediaId)),
			Map.of("text", "둘째 문단이다.", "sources", List.of())));
		body.put("articleSection", "경제");
		body.put("keywords", List.of("코스피", "반도체"));
		body.put("sourceMediaIds", List.of(mediaId));
		body.put("newsEventIds", events);
		body.put("issueIds", issues);
		body.put("generatorVersion", "test-1");
		return body;
	}

	private long upsert(Map<String, Object> body) throws Exception {
		return data(admin(MockMvcRequestBuilders.post("/api/v1/admin/article"), body)).path("articleId").asLong();
	}

	private JsonNode status(long articleId, String status) throws Exception {
		return data(admin(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}/status", articleId),
			Map.of("status", status, "approver", "desk")));
	}

	private List<Long> createEvents(Map<String, Object>... events) throws Exception {
		JsonNode linked = data(admin(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events", issueId),
			Map.of("proposalId", "p-" + UUID.randomUUID(), "events", List.of(events))));
		List<Long> ids = new ArrayList<>();
		linked.path("eventIds").forEach(id -> ids.add(id.asLong()));
		return ids;
	}

	@BeforeEach
	void setUp() throws Exception {
		mediaId = mediaJpaRepository.save(MediaEntity.builder().title("근거 기사").type("news").build()).getId();
		issueId = data(admin(MockMvcRequestBuilders.post("/api/v1/admin/issue"),
			Map.of("proposalId", "p-" + UUID.randomUUID(), "title", "아티클 이슈", "majorCategory", "경제"))).path("issueId").asLong();
	}

	@Nested
	@DisplayName("적재 → 게시 → 회수")
	class Lifecycle {

		@Test
		@DisplayName("REVIEW 로 적재되고 공개되지 않는다. 게시하면 공개되고 내부 근거는 안 나간다. 회수하면 다시 404")
		void lifecycle() throws Exception {
			String articleKey = key();
			JsonNode created = data(admin(MockMvcRequestBuilders.post("/api/v1/admin/article"), article(articleKey, "toronchul", List.of(), List.of(issueId))));
			long id = created.path("articleId").asLong();
			assertThat(created.path("status").asText()).isEqualTo("REVIEW");
			assertThat(anonymous(MockMvcRequestBuilders.get("/api/v1/article").param("article-id", String.valueOf(id)))
				.getResponse().getStatus()).isEqualTo(404);

			JsonNode published = status(id, "PUBLISHED");
			String datePublished = published.path("datePublished").asText();
			assertThat(datePublished).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

			JsonNode detail = data(anonymous(MockMvcRequestBuilders.get("/api/v1/article").param("article-id", String.valueOf(id))));
			assertThat(detail.path("headline").asText()).isEqualTo("코스피, 8,000선 돌파 뒤 하루 만에 급락");
			assertThat(detail.path("body")).hasSize(2);
			assertThat(detail.path("body").get(0).isTextual()).isTrue();
			assertThat(detail.toString()).doesNotContain("sources");
			assertThat(detail.path("byline").asText()).isEqualTo("토론철 AI 기자");
			assertThat(detail.path("aiGenerated").asBoolean()).isTrue();
			assertThat(detail.path("keywords")).hasSize(2);
			assertThat(detail.path("issueIds").get(0).asLong()).isEqualTo(issueId);
			assertThat(detail.path("updatedAt").isTextual()).isTrue();

			// 게시된 뒤 같은 키 재적재는 409
			assertThat(admin(MockMvcRequestBuilders.post("/api/v1/admin/article"), article(articleKey, "toronchul", List.of(), List.of()))
				.getResponse().getStatus()).isEqualTo(409);

			status(id, "HIDDEN");
			assertThat(anonymous(MockMvcRequestBuilders.get("/api/v1/article").param("article-id", String.valueOf(id)))
				.getResponse().getStatus()).isEqualTo(404);

			// 다시 게시해도 처음 게시 시각은 그대로
			assertThat(status(id, "PUBLISHED").path("datePublished").asText()).isEqualTo(datePublished);
		}

		@Test
		@DisplayName("게시 전 같은 키는 갱신, 같은 내용이면 changed=false")
		void upsertBeforePublish() throws Exception {
			String articleKey = key();
			Map<String, Object> body = article(articleKey, "toronchul", List.of(), List.of());
			long id = upsert(body);

			JsonNode same = data(admin(MockMvcRequestBuilders.post("/api/v1/admin/article"), body));
			assertThat(same.path("articleId").asLong()).isEqualTo(id);
			assertThat(same.path("changed").asBoolean()).isFalse();

			body.put("headline", "고친 제목");
			assertThat(data(admin(MockMvcRequestBuilders.post("/api/v1/admin/article"), body)).path("changed").asBoolean()).isTrue();
			assertThat(articleRepository.findById(id).orElseThrow().getHeadline()).isEqualTo("고친 제목");
		}

		@Test
		@DisplayName("없는 출처 media 가 섞이면 404 + 무저장")
		void unknownMedia() throws Exception {
			String articleKey = key();
			Map<String, Object> body = article(articleKey, "toronchul", List.of(), List.of());
			body.put("sourceMediaIds", List.of(mediaId, 987654321L));

			assertThat(admin(MockMvcRequestBuilders.post("/api/v1/admin/article"), body).getResponse().getStatus()).isEqualTo(404);
			assertThat(articleRepository.findByArticleKey(articleKey)).isEmpty();
		}

		@Test
		@DisplayName("열리지 않은 전이는 409 (REVIEW→APPROVED, PUBLISHED→REVIEW)")
		void transitions() throws Exception {
			long id = upsert(article(key(), "toronchul", List.of(), List.of()));
			assertThat(admin(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}/status", id), Map.of("status", "APPROVED"))
				.getResponse().getStatus()).isEqualTo(409);

			status(id, "PUBLISHED");
			assertThat(admin(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}/status", id), Map.of("status", "REVIEW"))
				.getResponse().getStatus()).isEqualTo(409);
			assertThat(status(id, "PUBLISHED").path("changed").asBoolean()).isFalse();
		}

		@Test
		@DisplayName("고객사판(tmoney)은 게시돼도 공개 API 에 안 나온다")
		void tmoneyNotPublic() throws Exception {
			long id = upsert(article(key(), "tmoney", List.of(), List.of()));
			status(id, "PUBLISHED");

			assertThat(anonymous(MockMvcRequestBuilders.get("/api/v1/article").param("article-id", String.valueOf(id)))
				.getResponse().getStatus()).isEqualTo(404);
		}
	}

	@Test
	@DisplayName("사건 타임라인: 발생일 내림차순·커서 페이징, 사건마다 게시 아티클, 병합된 사건의 아티클은 흡수한 사건 밑에")
	void timeline() throws Exception {
		List<Long> events = createEvents(
			Map.of("name", "가장 오래된 사건", "date", "2026-10-01"),
			Map.of("name", "가운데 사건", "date", "2026-10-05"),
			Map.of("name", "최신 사건", "date", "2026-10-10"));
		long oldest = events.get(0);
		long middle = events.get(1);
		long latest = events.get(2);

		long onLatest = upsert(article(key(), "toronchul", List.of(latest), List.of()));
		long onOldest = upsert(article(key(), "toronchul", List.of(oldest), List.of()));
		long unpublished = upsert(article(key(), "toronchul", List.of(latest), List.of()));
		status(onLatest, "PUBLISHED");
		status(onOldest, "PUBLISHED");

		JsonNode first = data(anonymous(MockMvcRequestBuilders.get("/api/v1/issue/events")
			.param("issue-id", String.valueOf(issueId)).param("size", "2")));
		assertThat(first.path("items")).hasSize(2);
		assertThat(first.path("items").get(0).path("eventId").asLong()).isEqualTo(latest);
		assertThat(first.path("items").get(1).path("eventId").asLong()).isEqualTo(middle);
		JsonNode latestCards = first.path("items").get(0).path("articles");
		assertThat(latestCards).hasSize(1);
		assertThat(latestCards.get(0).path("articleId").asLong()).isEqualTo(onLatest).isNotEqualTo(unpublished);
		assertThat(latestCards.get(0).path("summary").asText()).isNotBlank();

		JsonNode second = data(anonymous(MockMvcRequestBuilders.get("/api/v1/issue/events")
			.param("issue-id", String.valueOf(issueId)).param("size", "2").param("cursor", first.path("nextCursor").asText())));
		assertThat(second.path("items")).hasSize(1);
		assertThat(second.path("items").get(0).path("eventId").asLong()).isEqualTo(oldest);
		assertThat(second.path("nextCursor").isNull()).isTrue();

		// 가장 오래된 사건을 최신 사건에 병합 → 타임라인에서 빠지고 그 아티클은 최신 사건 밑에
		data(admin(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events/merge", issueId),
			Map.of("proposalId", "p-" + UUID.randomUUID(), "keepEventId", latest, "dropEventId", oldest)));
		JsonNode merged = data(anonymous(MockMvcRequestBuilders.get("/api/v1/issue/events").param("issue-id", String.valueOf(issueId))));
		assertThat(merged.path("items")).hasSize(2);
		List<Long> cards = new ArrayList<>();
		merged.path("items").get(0).path("articles").forEach(c -> cards.add(c.path("articleId").asLong()));
		assertThat(cards).containsExactlyInAnyOrder(onLatest, onOldest);

		// 단건의 events 도 흡수한 사건으로 보인다
		JsonNode detail = data(anonymous(MockMvcRequestBuilders.get("/api/v1/article").param("article-id", String.valueOf(onOldest))));
		assertThat(detail.path("events").get(0).path("eventId").asLong()).isEqualTo(latest);
		assertThat(detail.path("issueIds").get(0).asLong()).isEqualTo(issueId);
	}

	@Test
	@DisplayName("보관(ARCHIVED) 이슈의 타임라인은 404, 잘못된 cursor 는 400")
	void timelineGuards() throws Exception {
		assertThat(anonymous(MockMvcRequestBuilders.get("/api/v1/issue/events").param("issue-id", String.valueOf(issueId)).param("cursor", "garbage"))
			.getResponse().getStatus()).isEqualTo(400);

		data(admin(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId), Map.of("status", "ARCHIVED")));
		assertThat(anonymous(MockMvcRequestBuilders.get("/api/v1/issue/events").param("issue-id", String.valueOf(issueId)))
			.getResponse().getStatus()).isEqualTo(404);
	}

	@Test
	@DisplayName("연결 교체: 보낸 목록만 바뀐다")
	void replaceLinks() throws Exception {
		long event = createEvents(Map.of("name", "연결될 사건", "date", "2026-10-12")).get(0);
		long id = upsert(article(key(), "toronchul", List.of(), List.of(issueId)));

		JsonNode links = data(admin(MockMvcRequestBuilders.put("/api/v1/admin/article/{id}/links", id), Map.of("newsEventIds", List.of(event))));

		assertThat(links.path("newsEventIds").get(0).asLong()).isEqualTo(event);
		assertThat(links.path("issueIds").get(0).asLong()).isEqualTo(issueId);
	}

	@Test
	@DisplayName("사이트맵 피드는 게시된 토론철판만")
	void sitemap() throws Exception {
		long published = upsert(article(key(), "toronchul", List.of(), List.of()));
		long review = upsert(article(key(), "toronchul", List.of(), List.of()));
		status(published, "PUBLISHED");

		JsonNode feed = data(anonymous(MockMvcRequestBuilders.get("/api/v1/sitemap/articles")
			.param("cursor", String.valueOf(published - 1)).param("size", "5000")));
		List<Long> ids = new ArrayList<>();
		feed.path("items").forEach(i -> ids.add(i.path("articleId").asLong()));

		assertThat(ids).contains(published).doesNotContain(review);
		assertThat(feed.path("items").get(0).path("updatedAt").isTextual()).isTrue();
	}

	@Test
	@DisplayName("확인용 조회 (articleKey)")
	void findByKey() throws Exception {
		String articleKey = key();
		long id = upsert(article(articleKey, "toronchul", List.of(), List.of(issueId)));

		JsonNode found = data(admin(MockMvcRequestBuilders.get("/api/v1/admin/article").param("articleKey", articleKey), null));

		assertThat(found.path("articleId").asLong()).isEqualTo(id);
		assertThat(found.path("sourceCount").asInt()).isEqualTo(1);
	}
}
