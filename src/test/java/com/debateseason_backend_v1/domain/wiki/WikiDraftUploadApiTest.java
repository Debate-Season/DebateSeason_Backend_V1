package com.debateseason_backend_v1.domain.wiki;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.domain.wiki.domain.WikiSource;
import com.debateseason_backend_v1.domain.wiki.domain.WikiStatus;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRepository;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRevisionRepository;
import com.debateseason_backend_v1.security.jwt.JwtUtil;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 로컬 생성 초안 업로드 → ADMIN 게시 → 서빙(summary 포함). 서비스 키는 DRAFT 까지만, 게시는 사람만.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("위키 초안 업로드")
class WikiDraftUploadApiTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private IssueJpaRepository issueJpaRepository;
	@Autowired
	private DebateWikiRepository debateWikiRepository;
	@Autowired
	private DebateWikiRevisionRepository debateWikiRevisionRepository;

	private MvcResult call(MockHttpServletRequestBuilder request, Object body) throws Exception {
		return mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
			.andReturn();
	}

	private JsonNode data(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(body).isEqualTo(200);
		return objectMapper.readTree(body).path("data");
	}

	@Test
	@DisplayName("서비스 키로 올린 초안은 source=AI DRAFT, 같은 내용 재업로드는 새로 쌓지 않는다. 게시는 서비스 키로 안 되고 ADMIN 만 된다")
	void uploadPublishServe() throws Exception {
		Long issueId = issueJpaRepository.save(IssueEntity.builder().title("위키 이슈").build()).getId();
		Map<String, Object> draft = Map.of("issueId", issueId, "content", "## 개요\n본문", "summary", "두 문장 요약이다. 평문이다.",
			"model", "claude-opus-5-5", "editSummary", "로컬 생성");

		JsonNode first = data(call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/drafts")
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY), draft));
		long revisionId = first.path("revisionId").asLong();
		assertThat(first.path("status").asText()).isEqualTo("DRAFT");
		assertThat(first.path("source").asText()).isEqualTo("AI");
		assertThat(debateWikiRevisionRepository.findById(revisionId).orElseThrow().getSource()).isEqualTo(WikiSource.AI);

		JsonNode again = data(call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/drafts")
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY), draft));
		assertThat(again.path("revisionId").asLong()).isEqualTo(revisionId);

		// 미게시: 서빙은 비어 있다
		JsonNode before = data(mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/wiki").param("issue-id", String.valueOf(issueId))).andReturn());
		assertThat(before.path("content").isNull()).isTrue();
		assertThat(before.path("summary").isNull()).isTrue();

		// 파이프라인 키(ontology)로는 게시할 수 없다 — 403
		MvcResult byService = call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/publish")
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY), Map.of("revisionId", revisionId));
		assertThat(byService.getResponse().getStatus()).isEqualTo(403);
		assertThat(debateWikiRepository.findByIssueId(issueId).orElseThrow().getStatus()).isEqualTo(WikiStatus.DRAFT);

		data(call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/publish")
			.header("Authorization", "Bearer " + jwtUtil.createAccessToken(1L, UserRole.ADMIN)), Map.of("revisionId", revisionId)));

		JsonNode served = data(mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/wiki").param("issue-id", String.valueOf(issueId))).andReturn());
		assertThat(served.path("content").asText()).isEqualTo("## 개요\n본문");
		assertThat(served.path("summary").asText()).isEqualTo("두 문장 요약이다. 평문이다.");
		assertThat(served.path("model").asText()).isEqualTo("claude-opus-5-5");
		// 다른 응답과 같은 문자열 포맷 (배열 [2026,10,19,...] 로 나가 웹이 500 을 낸 적이 있다)
		assertThat(served.path("updatedAt").isTextual()).isTrue();
		assertThat(served.path("updatedAt").asText()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
	}

	@Test
	@DisplayName("model 이 빠지면 400")
	void modelRequired() throws Exception {
		Long issueId = issueJpaRepository.save(IssueEntity.builder().title("위키 이슈 2").build()).getId();

		MvcResult result = call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/drafts").header(ServiceApiKeyFilter.HEADER, SERVICE_KEY),
			Map.of("issueId", issueId, "content", "본문"));

		assertThat(result.getResponse().getStatus()).isEqualTo(400);
	}

	@Test
	@DisplayName("게시 전용 키(wiki-editor)로는 게시할 수 있다")
	void wikiEditorKeyCanPublish() throws Exception {
		Long issueId = issueJpaRepository.save(IssueEntity.builder().title("위키 이슈 3").build()).getId();
		long revisionId = data(call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/drafts").header(ServiceApiKeyFilter.HEADER, SERVICE_KEY),
			Map.of("issueId", issueId, "content", "본문 3", "model", "claude-opus-5-5"))).path("revisionId").asLong();

		data(call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/publish")
			.header(ServiceApiKeyFilter.HEADER, "test-wiki-editor-key-0123456789abcdef0123"), Map.of("revisionId", revisionId)));

		assertThat(debateWikiRepository.findByIssueId(issueId).orElseThrow().getStatus()).isEqualTo(WikiStatus.PUBLISHED);
	}
}
