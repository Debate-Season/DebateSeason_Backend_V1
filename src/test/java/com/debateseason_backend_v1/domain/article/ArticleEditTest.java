package com.debateseason_backend_v1.domain.article;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
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

import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** 게시된 아티클 수정: 보낸 필드만, 수정 전 리비전, 공개 응답 반영, 되돌리기·충돌, 멱등. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("아티클 수정")
class ArticleEditTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JdbcTemplate jdbc;

	private MvcResult admin(MockHttpServletRequestBuilder request, Object body) throws Exception {
		return mockMvc.perform(request.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY)
			.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))).andReturn();
	}

	private JsonNode data(MvcResult result) throws Exception {
		String text = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(text).isEqualTo(200);
		return objectMapper.readTree(text).path("data");
	}

	private long published() throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("articleKey", "test/toronchul/" + UUID.randomUUID());
		body.put("edition", "toronchul");
		body.put("headline", "원래 제목");
		body.put("summary", "원래 요약.");
		body.put("body", List.of(Map.of("text", "원래 첫 문단.", "sources", List.of()), Map.of("text", "원래 둘째 문단.", "sources", List.of())));
		body.put("articleSection", "사회");
		body.put("keywords", List.of("원래"));
		long id = data(admin(MockMvcRequestBuilders.post("/api/v1/admin/article"), body)).path("articleId").asLong();
		data(admin(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}/status", id), Map.of("status", "PUBLISHED")));
		return id;
	}

	private JsonNode publicArticle(long id) throws Exception {
		return data(mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/article").param("article-id", String.valueOf(id))).andReturn());
	}

	private JsonNode edit(long id, Map<String, Object> fields) throws Exception {
		Map<String, Object> body = new HashMap<>(fields);
		body.putIfAbsent("proposalId", "p-" + UUID.randomUUID());
		return data(admin(MockMvcRequestBuilders.patch("/api/v1/admin/article/{id}", id), body));
	}

	@Test
	@DisplayName("게시된 기사도 보낸 필드만 바뀐다. 수정 전 내용은 리비전에 남고, 되돌리면 원래대로")
	void editAndRevert() throws Exception {
		long id = published();
		jdbc.update("UPDATE toronchul_article SET updated_at = DATEADD('DAY', -1, CURRENT_TIMESTAMP) WHERE id = ?", id);
		String updatedBefore = publicArticle(id).path("updatedAt").asText();

		JsonNode edited = edit(id, Map.of("headline", "고친 제목",
			"body", List.of(Map.of("text", "고친 문단.", "sources", List.of())), "reason", "데스크 사실 지적 반영"));
		assertThat(edited.path("changed").asBoolean()).isTrue();
		assertThat(edited.path("revisionId").asLong()).isPositive();

		JsonNode after = publicArticle(id);
		assertThat(after.path("headline").asText()).isEqualTo("고친 제목");
		assertThat(after.path("body")).hasSize(1);
		assertThat(after.path("summary").asText()).isEqualTo("원래 요약.");
		assertThat(after.path("keywords").get(0).asText()).isEqualTo("원래");
		assertThat(after.path("updatedAt").asText()).isNotEqualTo(updatedBefore);

		Integer revisions = jdbc.queryForObject("SELECT COUNT(*) FROM toronchul_article_revision WHERE article_id = ? AND headline = '원래 제목'",
			Integer.class, id);
		assertThat(revisions).isEqualTo(1);

		data(admin(MockMvcRequestBuilders.post("/api/v1/admin/change-log/{id}/revert", edited.path("changeLogId").asLong()), Map.of()));
		JsonNode reverted = publicArticle(id);
		assertThat(reverted.path("headline").asText()).isEqualTo("원래 제목");
		assertThat(reverted.path("body")).hasSize(2);
	}

	@Test
	@DisplayName("그 뒤 또 고쳐졌으면 앞선 수정은 되돌리지 않는다 (409)")
	void revertConflict() throws Exception {
		long id = published();
		long first = edit(id, Map.of("headline", "두 번째")).path("changeLogId").asLong();
		edit(id, Map.of("headline", "세 번째"));

		MvcResult result = admin(MockMvcRequestBuilders.post("/api/v1/admin/change-log/{id}/revert", first), Map.of());

		assertThat(result.getResponse().getStatus()).isEqualTo(409);
		assertThat(publicArticle(id).path("headline").asText()).isEqualTo("세 번째");
	}

	@Test
	@DisplayName("같은 proposalId 재호출은 처음 결과, 바뀔 게 없으면 changed=false")
	void idempotent() throws Exception {
		long id = published();
		String proposalId = "p-" + UUID.randomUUID();

		JsonNode first = edit(id, Map.of("proposalId", proposalId, "summary", "새 요약."));
		JsonNode again = edit(id, Map.of("proposalId", proposalId, "summary", "새 요약."));
		assertThat(again.path("changed").asBoolean()).isFalse();
		assertThat(again.path("changeLogId").asLong()).isEqualTo(first.path("changeLogId").asLong());

		assertThat(edit(id, Map.of("summary", "새 요약.")).path("changed").asBoolean()).isFalse();
	}
}
