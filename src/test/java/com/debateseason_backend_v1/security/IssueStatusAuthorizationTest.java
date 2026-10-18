package com.debateseason_backend_v1.security;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueStatusHistory;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueStatusHistoryRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.security.jwt.JwtUtil;
import com.debateseason_backend_v1.security.service.ServiceApiKeyFilter;

/**
 * PATCH /api/v1/admin/issue/{id}/status 의 인증·인가를 실제 필터 체인으로 검증한다.
 *
 * 서비스 키가 통하는 곳은 이 경로뿐이어야 한다. 키가 새도 다른 ADMIN API 는 열리면 안 된다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("이슈 상태 변경 API 인증")
class IssueStatusAuthorizationTest {

	// application-test.yml 의 테스트 전용 키
	private static final String SERVICE_KEY = "test-service-key-0123456789abcdef0123456789";
	private static final String BODY = "{\"status\":\"DORMANT\",\"reason\":\"14일간 새 사건 없음\",\"approver\":\"123456789\"}";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private IssueJpaRepository issueJpaRepository;
	@Autowired
	private IssueStatusHistoryRepository issueStatusHistoryRepository;

	private Long issueId;

	@BeforeEach
	void setUp() {
		issueStatusHistoryRepository.deleteAll();
		issueId = issueJpaRepository.save(IssueEntity.builder().title("잠재울 이슈").majorCategory("정치").build()).getId();
	}

	private MockHttpServletRequestBuilder patch(Long id) {
		return MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(BODY);
	}

	private MvcResult perform(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request).andReturn();
	}

	@Test
	@DisplayName("서비스 키로 잠재우면 상태가 바뀌고 decided_by=service:ontology 이력이 남는다")
	void serviceKeySucceeds() throws Exception {
		MvcResult result = perform(patch(issueId).header(ServiceApiKeyFilter.HEADER, SERVICE_KEY));

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		assertThat(issueJpaRepository.findById(issueId).orElseThrow().getStatus()).isEqualTo(IssueStatus.DORMANT);

		List<IssueStatusHistory> history = issueStatusHistoryRepository.findAll();
		assertThat(history).hasSize(1);
		assertThat(history.get(0).getDecidedBy()).isEqualTo("service:ontology");
		assertThat(history.get(0).getApprover()).isEqualTo("123456789");
	}

	@Test
	@DisplayName("ADMIN JWT 도 쓸 수 있고 decided_by=user:{id} 로 남는다")
	void adminJwtSucceeds() throws Exception {
		MvcResult result = perform(patch(issueId)
			.header("Authorization", "Bearer " + jwtUtil.createAccessToken(7L, UserRole.ADMIN)));

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		assertThat(issueStatusHistoryRepository.findAll()).extracting(IssueStatusHistory::getDecidedBy)
			.containsExactly("user:7");
	}

	@Test
	@DisplayName("틀린 서비스 키는 JWT 로 넘어가지 않고 바로 401 (INVALID_SERVICE_API_KEY)")
	void wrongServiceKey() throws Exception {
		MvcResult result = perform(patch(issueId).header(ServiceApiKeyFilter.HEADER, "wrong-key"));

		assertThat(result.getResponse().getStatus()).isEqualTo(401);
		assertThat(result.getResponse().getContentAsString()).contains("INVALID_SERVICE_API_KEY");
		assertThat(issueJpaRepository.findById(issueId).orElseThrow().getStatus()).isEqualTo(IssueStatus.PUBLISHED);
	}

	@Test
	@DisplayName("아무 인증도 없으면 401")
	void anonymous() throws Exception {
		assertThat(perform(patch(issueId)).getResponse().getStatus()).isEqualTo(401);
	}

	@Test
	@DisplayName("USER 는 403")
	void user() throws Exception {
		MvcResult result = perform(patch(issueId)
			.header("Authorization", "Bearer " + jwtUtil.createAccessToken(1L, UserRole.USER)));

		assertThat(result.getResponse().getStatus()).isEqualTo(403);
	}

	@Test
	@DisplayName("서비스 키는 다른 ADMIN API 를 열지 못한다")
	void serviceKeyDoesNotOpenOtherAdminApis() throws Exception {
		MvcResult result = perform(MockMvcRequestBuilders.post("/api/v1/admin/wiki/publish")
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"revisionId\":1}"));

		// 헤더를 무시하므로 "토큰 없음" 401 이다. 인가를 통과했다면 404/200 이 났을 것이다.
		assertThat(result.getResponse().getStatus()).isEqualTo(401);
	}

	@Test
	@DisplayName("웹 SSR 키는 관리 API 를 열지 못한다 — X-Service-Key 로 보내도, 자기 헤더로 보내도 401")
	void webSsrKeyIsNotAServiceKey() throws Exception {
		String webKey = "test-web-ssr-key-0123456789abcdef012345";

		assertThat(perform(patch(issueId).header(ServiceApiKeyFilter.HEADER, webKey)).getResponse().getStatus())
			.isEqualTo(401);
		assertThat(perform(patch(issueId).header("X-Toronchul-Web-Key", webKey)).getResponse().getStatus())
			.isEqualTo(401);
		assertThat(issueJpaRepository.findById(issueId).orElseThrow().getStatus()).isEqualTo(IssueStatus.PUBLISHED);
	}

	@Test
	@DisplayName("서비스 키 경로라도 GET 은 받지 않는다")
	void serviceKeyOnlyForPatch() throws Exception {
		MvcResult result = perform(MockMvcRequestBuilders.get("/api/v1/admin/issue/{id}/status", issueId)
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY));

		assertThat(result.getResponse().getStatus()).isEqualTo(401);
	}

	@Test
	@DisplayName("허용되지 않는 전이는 409")
	void invalidTransition() throws Exception {
		MvcResult result = perform(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId)
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"status\":\"DRAFT\"}"));

		assertThat(result.getResponse().getStatus()).isEqualTo(409);
	}

	@Test
	@DisplayName("status 가 빠지면 400")
	void missingStatus() throws Exception {
		MvcResult result = perform(MockMvcRequestBuilders.patch("/api/v1/admin/issue/{id}/status", issueId)
			.header(ServiceApiKeyFilter.HEADER, SERVICE_KEY)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"reason\":\"x\"}"));

		assertThat(result.getResponse().getStatus()).isEqualTo(400);
	}
}
