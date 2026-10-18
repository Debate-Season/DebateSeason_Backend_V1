package com.debateseason_backend_v1.domain.issue;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;

/**
 * GET /api/v1/issue 상세의 404.
 *
 * 상세 쿼리는 COUNT 집계라 없는 이슈에도 NULL 행을 하나 돌려준다. 행 유무로 판단하면 200 + title null 이 나가고,
 * 앱 DTO 는 title 이 non-null 이라 화면이 죽는다. 운영에서 issue-id=1 (삭제된 이슈) 이 실제로 그렇게 나갔다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("이슈 상세 404")
class IssueDetailNotFoundTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private IssueJpaRepository issueJpaRepository;

	private MvcResult fetch(long issueId) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/issue").param("issue-id", String.valueOf(issueId)))
			.andReturn();
	}

	@Test
	@DisplayName("없는 이슈는 200 + title null 이 아니라 404 NOT_FOUND_ISSUE")
	void missingIssue() throws Exception {
		MvcResult result = fetch(987654321L);

		assertThat(result.getResponse().getStatus()).isEqualTo(404);
		assertThat(result.getResponse().getContentAsString()).contains("NOT_FOUND_ISSUE");
	}

	@Test
	@DisplayName("ARCHIVED 이슈도 404")
	void archivedIssue() throws Exception {
		Long id = issueJpaRepository.save(IssueEntity.builder().title("롤백된 이슈").status(IssueStatus.ARCHIVED).build()).getId();

		assertThat(fetch(id).getResponse().getStatus()).isEqualTo(404);
	}

	@Test
	@DisplayName("있는 이슈는 200 (DORMANT 포함)")
	void existingIssue() throws Exception {
		Long published = issueJpaRepository.save(IssueEntity.builder().title("있는 이슈").build()).getId();
		Long dormant = issueJpaRepository.save(IssueEntity.builder().title("잠자는 이슈").status(IssueStatus.DORMANT).build()).getId();

		MvcResult ok = fetch(published);
		assertThat(ok.getResponse().getStatus()).isEqualTo(200);
		// 토론 주제가 없는 이슈: chatRoomMap 은 null 이 아니라 빈 배열 (null 이면 웹 상세가 500 이었다)
		assertThat(ok.getResponse().getContentAsString()).contains("\"chatRoomMap\":[]");
		assertThat(fetch(dormant).getResponse().getStatus()).isEqualTo(200);
	}
}
