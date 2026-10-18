package com.debateseason_backend_v1.security.docs;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * API 문서 접근: 로그인 없으면 401, 팀 계정은 전체, 고객사 계정은 고객사 그룹만.
 * 그룹이 실제로 나뉘었는지(내부 경로가 고객사·서비스 문서에 새지 않는지)도 본다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("API 문서 접근 제어")
class DocsSecurityTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	private static String basic(String user, String password) {
		return "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
	}

	private static final String TEAM = basic("team", "test-team-pw");
	private static final String PARTNER = basic("tmoney", "test-partner-pw");

	private MvcResult get(String path, String auth) throws Exception {
		var request = MockMvcRequestBuilders.get(path);
		if (auth != null) {
			request = request.header("Authorization", auth);
		}
		return mockMvc.perform(request).andReturn();
	}

	private int status(String path, String auth) throws Exception {
		return get(path, auth).getResponse().getStatus();
	}

	@Test
	@DisplayName("로그인 없이는 문서·UI 모두 401 (브라우저 로그인 창)")
	void anonymous() throws Exception {
		for (String path : new String[] {"/v3/api-docs", "/v3/api-docs/toronchul", "/v3/api-docs/admin",
			"/v3/api-docs/partner", "/swagger-ui/index.html", "/partner/docs"}) {
			MvcResult result = get(path, null);
			assertThat(result.getResponse().getStatus()).as(path).isEqualTo(401);
		}
		assertThat(get("/v3/api-docs/toronchul", null).getResponse().getHeader("WWW-Authenticate")).startsWith("Basic");
	}

	@Test
	@DisplayName("틀린 비밀번호는 401")
	void wrongPassword() throws Exception {
		assertThat(status("/v3/api-docs/toronchul", basic("team", "nope"))).isEqualTo(401);
	}

	@Test
	@DisplayName("팀 계정은 세 그룹 모두")
	void team() throws Exception {
		assertThat(status("/v3/api-docs/toronchul", TEAM)).isEqualTo(200);
		assertThat(status("/v3/api-docs/admin", TEAM)).isEqualTo(200);
		assertThat(status("/v3/api-docs/partner", TEAM)).isEqualTo(200);
		assertThat(status("/v3/api-docs/swagger-config", TEAM)).isEqualTo(200);
	}

	@Test
	@DisplayName("고객사 계정은 고객사 그룹과 UI 만, 내부 그룹은 403")
	void partner() throws Exception {
		assertThat(status("/v3/api-docs/partner", PARTNER)).isEqualTo(200);
		assertThat(status("/v3/api-docs/swagger-config", PARTNER)).isEqualTo(200);
		assertThat(status("/swagger-ui/index.html", PARTNER)).isEqualTo(200);
		assertThat(status("/v3/api-docs/toronchul", PARTNER)).isEqualTo(403);
		assertThat(status("/v3/api-docs/admin", PARTNER)).isEqualTo(403);
		assertThat(status("/v3/api-docs", PARTNER)).isEqualTo(403);
	}

	@Test
	@DisplayName("고객사 문서 주소는 고객사 그룹으로 연 Swagger UI 로 보낸다")
	void partnerDocsRedirect() throws Exception {
		MvcResult result = get("/partner/docs", PARTNER);

		assertThat(result.getResponse().getStatus()).isEqualTo(302);
		assertThat(result.getResponse().getRedirectedUrl()).contains("/swagger-ui/index.html?urls.primaryName=");
	}

	@Test
	@DisplayName("그룹 분리: 서비스 문서엔 관리 경로가 없고, 관리 문서엔 관리 경로만 있다")
	void groupsAreSplit() throws Exception {
		JsonNode service = objectMapper.readTree(get("/v3/api-docs/toronchul", TEAM).getResponse().getContentAsString(StandardCharsets.UTF_8));
		JsonNode admin = objectMapper.readTree(get("/v3/api-docs/admin", TEAM).getResponse().getContentAsString(StandardCharsets.UTF_8));

		service.path("paths").fieldNames().forEachRemaining(p -> assertThat(p).doesNotStartWith("/api/v1/admin").doesNotStartWith("/api/partner"));
		admin.path("paths").fieldNames().forEachRemaining(p -> assertThat(p).startsWith("/api/v1/admin"));
		assertThat(service.path("paths").has("/api/v1/article")).isTrue();
		assertThat(admin.path("paths").has("/api/v1/admin/article")).isTrue();
		assertThat(admin.path("components").path("securitySchemes").has("ServiceKey")).isTrue();
	}

	@Test
	@DisplayName("같은 이름 record 가 한 스키마로 합쳐지지 않는다 — 공개 아티클의 사건은 eventId·issueIds 를 가진다")
	void schemaNamesDoNotCollide() throws Exception {
		JsonNode schemas = objectMapper.readTree(get("/v3/api-docs/toronchul", TEAM).getResponse()
			.getContentAsString(StandardCharsets.UTF_8)).path("components").path("schemas");

		JsonNode event = schemas.path("ArticleEvent").path("properties");
		assertThat(event.has("eventId")).isTrue();
		assertThat(event.has("issueIds")).isTrue();
		assertThat(event.has("mediaIds")).isFalse();
		assertThat(schemas.path("IssueTimelineEvent").path("properties").path("date").path("nullable").asBoolean()).isTrue();
	}

	@Test
	@DisplayName("문서 로그인은 앱 API 에 영향이 없다 (앱 API 는 Basic 을 보지 않는다)")
	void appApisUnaffected() throws Exception {
		assertThat(status("/api/v1/sitemap/issues", null)).isEqualTo(200);
		assertThat(status("/api/v1/sitemap/issues", TEAM)).isEqualTo(200);
	}
}
