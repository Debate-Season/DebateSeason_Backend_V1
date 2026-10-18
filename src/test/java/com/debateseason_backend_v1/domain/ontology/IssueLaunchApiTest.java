package com.debateseason_backend_v1.domain.ontology;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
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

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.wiki.domain.WikiStatus;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRepository;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 이슈는 위키·사건 타임라인과 함께 공개한다 (사용자 결정 2026-10-19).
 * DRAFT 로 만들고 → 사건·위키를 붙이고 → launch 한 번에 위키 게시 + 이슈 공개. 데이터 레포가 부르는 경로 그대로.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("이슈 공개 (launch)")
class IssueLaunchApiTest {

	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private IssueJpaRepository issueJpaRepository;
	@Autowired
	private DebateWikiRepository wikiRepository;

	private MvcResult call(MockHttpServletRequestBuilder request, Object body) throws Exception {
		return mockMvc.perform(request.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY)
			.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))).andReturn();
	}

	private JsonNode data(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(body).isEqualTo(200);
		return objectMapper.readTree(body).path("data");
	}

	private String code(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("code").asText();
	}

	private static String proposal() {
		return "p-" + UUID.randomUUID();
	}

	private long draftIssue() throws Exception {
		return data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue"),
			Map.of("proposalId", proposal(), "title", "공개 전 이슈", "majorCategory", "사회", "status", "DRAFT"))).path("issueId").asLong();
	}

	private long wikiDraft(long issueId) throws Exception {
		return data(call(MockMvcRequestBuilders.post("/api/v1/admin/wiki/drafts"),
			Map.of("issueId", issueId, "content", "## 개요\n본문 " + issueId, "summary", "요약.", "model", "claude-opus-5-5")))
			.path("revisionId").asLong();
	}

	private void addEvent(long issueId) throws Exception {
		data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/events", issueId),
			Map.of("proposalId", proposal(), "events", List.of(Map.of("name", "첫 사건", "date", "2026-10-19")))));
	}

	private MvcResult launch(long issueId, long revisionId, String proposalId) throws Exception {
		return call(MockMvcRequestBuilders.post("/api/v1/admin/issue/{id}/launch", issueId),
			Map.of("proposalId", proposalId, "wikiRevisionId", revisionId, "approver", "123456789"));
	}

	@Test
	@DisplayName("DRAFT 로 만들면 목록에 없고, 사건·위키를 붙여 launch 하면 위키 게시 + 공개, 롤백하면 다시 숨는다")
	void fullFlow() throws Exception {
		long issueId = draftIssue();
		assertThat(issueJpaRepository.findById(issueId).orElseThrow().getStatus()).isEqualTo(IssueStatus.DRAFT);
		long revision = wikiDraft(issueId);
		assertThat(issueJpaRepository.findTop6Issues()).doesNotContain(issueId);

		// 사건이 없으면 공개할 수 없다
		MvcResult noEvents = launch(issueId, revision, proposal());
		assertThat(noEvents.getResponse().getStatus()).isEqualTo(409);
		assertThat(code(noEvents)).isEqualTo("ISSUE_NOT_LAUNCHABLE");

		addEvent(issueId);
		String proposalId = proposal();
		JsonNode launched = data(launch(issueId, revision, proposalId));
		assertThat(launched.path("status").asText()).isEqualTo("PUBLISHED");
		assertThat(launched.path("eventCount").asInt()).isEqualTo(1);
		assertThat(issueJpaRepository.findById(issueId).orElseThrow().getStatus()).isEqualTo(IssueStatus.PUBLISHED);
		assertThat(wikiRepository.findByIssueId(issueId).orElseThrow().getStatus()).isEqualTo(WikiStatus.PUBLISHED);
		assertThat(wikiRepository.findByIssueId(issueId).orElseThrow().getPublishedRevisionId()).isEqualTo(revision);
		assertThat(issueJpaRepository.findTop6Issues()).contains(issueId);

		// 재시도는 멱등, 다른 proposalId 로 다시 공개는 409 (이미 공개됨)
		assertThat(data(launch(issueId, revision, proposalId)).path("changed").asBoolean()).isFalse();
		assertThat(launch(issueId, revision, proposal()).getResponse().getStatus()).isEqualTo(409);

		// 롤백: DRAFT 로 다시 숨긴다
		data(call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId), Map.of("status", "DRAFT")));
		assertThat(issueJpaRepository.findTop6Issues()).doesNotContain(issueId);
	}

	@Test
	@DisplayName("다른 이슈의 위키 리비전으로는 공개할 수 없다")
	void foreignRevision() throws Exception {
		long issueId = draftIssue();
		addEvent(issueId);
		long other = draftIssue();
		long otherRevision = wikiDraft(other);
		wikiDraft(issueId);

		MvcResult result = launch(issueId, otherRevision, proposal());

		assertThat(result.getResponse().getStatus()).isEqualTo(409);
		assertThat(issueJpaRepository.findById(issueId).orElseThrow().getStatus()).isEqualTo(IssueStatus.DRAFT);
	}

	@Test
	@DisplayName("DRAFT 이슈를 상태 API 로 바로 공개할 수 없다 (409 ISSUE_LAUNCH_REQUIRED), 버리기(ARCHIVED)는 된다")
	void statusApiCannotLaunch() throws Exception {
		long issueId = draftIssue();

		MvcResult publish = call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId), Map.of("status", "PUBLISHED"));
		assertThat(publish.getResponse().getStatus()).isEqualTo(409);
		assertThat(code(publish)).isEqualTo("ISSUE_LAUNCH_REQUIRED");

		data(call(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId), Map.of("status", "ARCHIVED")));
		assertThat(issueJpaRepository.findById(issueId).map(IssueEntity::getStatus).orElseThrow()).isEqualTo(IssueStatus.ARCHIVED);
	}

	@Test
	@DisplayName("status 를 비우면 지금처럼 PUBLISHED, DORMANT 로 만들기는 400")
	void createStatus() throws Exception {
		long published = data(call(MockMvcRequestBuilders.post("/api/v1/admin/issue"),
			Map.of("proposalId", proposal(), "title", "바로 공개", "majorCategory", "사회"))).path("issueId").asLong();
		assertThat(issueJpaRepository.findById(published).orElseThrow().getStatus()).isEqualTo(IssueStatus.PUBLISHED);

		MvcResult dormant = call(MockMvcRequestBuilders.post("/api/v1/admin/issue"),
			Map.of("proposalId", proposal(), "title", "x", "majorCategory", "사회", "status", "DORMANT"));
		assertThat(dormant.getResponse().getStatus()).isEqualTo(400);
	}
}
