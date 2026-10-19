package com.debateseason_backend_v1.domain.partner;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.debateseason_backend_v1.domain.partner.entity.PartnerArticleView;
import com.debateseason_backend_v1.domain.partner.entity.PartnerUsageDaily;
import com.debateseason_backend_v1.domain.partner.repository.PartnerArticleViewRepository;
import com.debateseason_backend_v1.domain.partner.repository.PartnerUsageDailyRepository;
import com.debateseason_backend_v1.domain.partner.service.PartnerCounters;
import com.debateseason_backend_v1.security.partner.PartnerApiKeyFilter;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 고객사(티머니GO) 뉴스 API: 고객사 키 인증, tmoney 판만, 키워드 정확 일치, 인기·최신, 고객사별 조회 수, 미터링.
 * 기사는 데이터 레포가 쓰는 실제 적재 API(서비스 키)로 넣는다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("고객사 뉴스 API")
class PartnerNewsApiTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";
	private static final String PARTNER_KEY = "test-partner-key-0123456789abcdef01234";
	private static final String PARTNER = "tmoney-go";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private PartnerCounters counters;
	@Autowired
	private PartnerArticleViewRepository viewRepository;
	@Autowired
	private PartnerUsageDailyRepository usageRepository;
	@Autowired
	private JdbcTemplate jdbc;

	private JsonNode data(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(body).isEqualTo(200);
		return objectMapper.readTree(body).path("data");
	}

	private MvcResult partner(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.header(PartnerApiKeyFilter.HEADER, PARTNER_KEY)).andReturn();
	}

	/** 데이터 레포처럼 적재하고 (선택) 게시한다. */
	private long load(String edition, List<String> keywords, boolean publish) throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("articleKey", "test/" + edition + "/" + UUID.randomUUID());
		body.put("edition", edition);
		body.put("headline", "강남역 출구 공사, 11월 말까지");
		body.put("summary", "강남역 11번 출구가 공사로 막힌다.");
		body.put("body", List.of(Map.of("text", "첫 문단.", "sources", List.of()), Map.of("text", "둘째 문단.", "sources", List.of())));
		body.put("articleSection", "사회");
		body.put("keywords", keywords);
		long id = data(mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/article")
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY).contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(body))).andReturn()).path("articleId").asLong();
		if (publish) {
			data(mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}/status", id)
				.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY).contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"PUBLISHED\"}")).andReturn());
		}
		return id;
	}

	private static List<Long> ids(JsonNode page) {
		List<Long> ids = new ArrayList<>();
		page.path("items").forEach(i -> ids.add(i.path("newsId").asLong()));
		return ids;
	}

	@Test
	@DisplayName("고객사 키가 없거나 틀리면 401. 서비스 키·웹 키로는 들어올 수 없다")
	void auth() throws Exception {
		MvcResult none = mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/v1/news")).andReturn();
		assertThat(none.getResponse().getStatus()).isEqualTo(401);
		assertThat(none.getResponse().getContentAsString()).contains("INVALID_PARTNER_API_KEY");

		assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/v1/news")
			.header(PartnerApiKeyFilter.HEADER, "wrong")).andReturn().getResponse().getStatus()).isEqualTo(401);
		assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/v1/news")
			.header(PartnerApiKeyFilter.HEADER, SERVICE_KEY)).andReturn().getResponse().getStatus()).isEqualTo(401);
		// 고객사 키는 관리 API 를 열지 못한다
		assertThat(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/article").param("articleKey", "x")
			.header(ServiceApiKeyFilter.HEADER, PARTNER_KEY)).andReturn().getResponse().getStatus()).isEqualTo(401);
	}

	@Test
	@DisplayName("키워드는 정확 일치만, 앞뒤 공백 무시. tmoney 판·게시본만 나온다. 맞는 게 없으면 빈 목록")
	void keyword() throws Exception {
		String place = "역" + UUID.randomUUID().toString().substring(0, 8);
		long tmoney = load("tmoney", List.of(place, "서울시"), true);
		long toronchul = load("toronchul", List.of(place), true);
		long review = load("tmoney", List.of(place), false);

		JsonNode page = data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news").param("keyword", "  " + place + " ")));
		assertThat(ids(page)).containsExactly(tmoney).doesNotContain(toronchul, review);

		JsonNode item = page.path("items").get(0);
		assertThat(item.path("title").asText()).isNotBlank();
		assertThat(item.path("summary").asText()).isNotBlank();
		assertThat(item.path("category").asText()).isEqualTo("사회");
		assertThat(item.path("keywords").get(0).asText()).isEqualTo(place);
		assertThat(item.path("publishedAt").asText()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
		assertThat(item.path("aiGenerated").asBoolean()).isTrue();
		// 목록에도 필수 7항목이 모두 있다 (본문 포함)
		assertThat(item.path("body")).hasSize(2);
		assertThat(item.path("viewCount").isNumber()).isTrue();

		// 부분 일치는 하지 않는다 ("강남" 이 "강남구" 를 잡으면 안 된다)
		JsonNode partial = data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news").param("keyword", place.substring(0, 4))));
		assertThat(partial.path("items")).isEmpty();
		assertThat(partial.path("nextCursor").isNull()).isTrue();
	}

	@Test
	@DisplayName("상세: 본문·AI 표시, 볼 때마다 조회 수가 오르고 목록에도 반영. 1분 반영 뒤 DB 에 남는다")
	void detailAndViews() throws Exception {
		String place = "터미널" + UUID.randomUUID().toString().substring(0, 6);
		long id = load("tmoney", List.of(place), true);

		JsonNode first = data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news/{id}", id)));
		assertThat(first.path("body")).hasSize(2);
		assertThat(first.path("aiGenerated").asBoolean()).isTrue();
		assertThat(first.path("viewCount").asLong()).isEqualTo(1);
		assertThat(data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news/{id}", id))).path("viewCount").asLong()).isEqualTo(2);

		JsonNode listed = data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news").param("keyword", place)));
		assertThat(listed.path("items").get(0).path("viewCount").asLong()).isEqualTo(2);

		counters.flush();
		assertThat(viewRepository.findById(new PartnerArticleView.Key(PARTNER, id)).orElseThrow().getViews()).isEqualTo(2);
		assertThat(usageRepository.findById(new PartnerUsageDaily.Key(PARTNER, LocalDate.now(), "news.detail"))
			.orElseThrow().getCalls()).isGreaterThanOrEqualTo(2);
		assertThat(usageRepository.findById(new PartnerUsageDaily.Key(PARTNER, LocalDate.now(), "news.list"))
			.orElseThrow().getCalls()).isGreaterThanOrEqualTo(1);
	}

	@Test
	@DisplayName("상세: 다른 판·미게시·없는 id 는 404")
	void detailNotFound() throws Exception {
		long toronchul = load("toronchul", List.of("x"), true);
		long review = load("tmoney", List.of("x"), false);

		for (long id : new long[] {toronchul, review, 987654321L}) {
			MvcResult result = partner(MockMvcRequestBuilders.get("/api/partner/v1/news/{id}", id));
			assertThat(result.getResponse().getStatus()).as("id " + id).isEqualTo(404);
		}
	}

	@Test
	@DisplayName("키워드 없으면 인기순: 같은 시기면 이 고객사 조회 수가 많은 쪽이 위. 7일 지난 기사는 빠진다")
	void popular() throws Exception {
		long quiet = load("tmoney", List.of("인기"), true);
		long hot = load("tmoney", List.of("인기"), true);
		long old = load("tmoney", List.of("인기"), true);
		jdbc.update("UPDATE toronchul_article SET date_published = DATEADD('DAY', -8, CURRENT_TIMESTAMP) WHERE id = ?", old);
		for (int i = 0; i < 5; i++) {
			partner(MockMvcRequestBuilders.get("/api/partner/v1/news/{id}", hot));
		}

		List<Long> ranked = ids(data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news").param("size", "50"))));

		assertThat(ranked).contains(hot, quiet).doesNotContain(old);
		assertThat(ranked.indexOf(hot)).isLessThan(ranked.indexOf(quiet));
	}

	@Test
	@DisplayName("최신순 커서: 빠짐·중복 없이 끝까지, 잘못된 cursor 는 400")
	void latestPaging() throws Exception {
		String place = "노선" + UUID.randomUUID().toString().substring(0, 6);
		List<Long> loaded = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			loaded.add(load("tmoney", List.of(place), true));
		}

		List<Long> seen = new ArrayList<>();
		String cursor = null;
		int calls = 0;
		do {
			MockHttpServletRequestBuilder req = MockMvcRequestBuilders.get("/api/partner/v1/news").param("keyword", place).param("size", "2");
			if (cursor != null) {
				req = req.param("cursor", cursor);
			}
			JsonNode page = data(partner(req));
			seen.addAll(ids(page));
			cursor = page.path("nextCursor").isNull() ? null : page.path("nextCursor").asText();
			calls++;
		} while (cursor != null && calls < 10);

		assertThat(seen).containsExactlyInAnyOrderElementsOf(loaded).doesNotHaveDuplicates();
		assertThat(seen.get(0)).isEqualTo(loaded.get(2));
		assertThat(partner(MockMvcRequestBuilders.get("/api/partner/v1/news").param("keyword", place).param("cursor", "garbage"))
			.getResponse().getStatus()).isEqualTo(400);
	}

	@Test
	@DisplayName("게시된 기사의 키워드만 교체하면 키워드 검색이 따라온다 (본문은 그대로)")
	void replaceKeywords() throws Exception {
		String wrong = "수산" + UUID.randomUUID().toString().substring(0, 4);
		String right = "부산" + UUID.randomUUID().toString().substring(0, 4);
		long id = load("tmoney", List.of(right, wrong), true);
		assertThat(ids(data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news").param("keyword", wrong))))).containsExactly(id);

		JsonNode changed = data(mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}", id)
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY).contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of("proposalId", "p-" + UUID.randomUUID(), "keywords", List.of(" " + right + " "),
				"reason", "오탐 제거")))).andReturn());
		assertThat(changed.path("changed").asBoolean()).isTrue();

		assertThat(data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news").param("keyword", wrong))).path("items")).isEmpty();
		JsonNode detail = data(partner(MockMvcRequestBuilders.get("/api/partner/v1/news/{id}", id)));
		assertThat(detail.path("keywords")).hasSize(1);
		assertThat(detail.path("body")).hasSize(2);

		// 같은 목록이면 무동작
		assertThat(data(mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}", id)
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY).contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(Map.of("proposalId", "p-" + UUID.randomUUID(), "keywords", List.of(right)))))
			.andReturn()).path("changed").asBoolean()).isFalse();
	}
}
