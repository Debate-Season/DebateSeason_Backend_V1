package com.debateseason_backend_v1.domain.issue.infrastructure.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.manager.IssueProcessorManager;
import com.debateseason_backend_v1.domain.issue.mapper.IssueBriefResponse;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;

import jakarta.persistence.EntityManager;

/**
 * 이슈 목록·홈·상세의 네이티브 쿼리는 Object[] 를 인덱스로 읽는다. 열 순서가 곧 계약이다.
 * status 를 끝에 붙였으므로 기존 인덱스가 그대로인지, status 가 제자리에서 나오는지 고정한다.
 */
@ActiveProfiles("test")
@DataJpaTest
@DisplayName("이슈 쿼리의 status 열")
class IssueStatusColumnQueryTest {

	@Autowired
	private EntityManager em;

	@Autowired
	private IssueJpaRepository issueJpaRepository;

	private Long persist(String title, IssueStatus status) {
		IssueEntity issue = IssueEntity.builder().title(title).majorCategory("정치").status(status).build();
		em.persist(issue);
		em.flush();

		// 슬라이스 테스트에서는 auditing 이 켜지지 않을 수 있다 (IssueMapOrderingTest 참고).
		// 응답 변환이 created_at 을 파싱하므로 값을 직접 넣는다.
		em.createNativeQuery("UPDATE issue SET created_at = CURRENT_TIMESTAMP WHERE issue_id = :id")
			.setParameter("id", issue.getId())
			.executeUpdate();
		return issue.getId();
	}

	@Test
	@DisplayName("목록 쿼리: 기존 0~4 열은 그대로, status 는 e[5]")
	void listQueries() {
		Long awake = persist("깨어 있는 이슈", IssueStatus.PUBLISHED);
		Long dormant = persist("잠자는 이슈", IssueStatus.DORMANT);

		for (List<Object[]> rows : List.of(
			issueJpaRepository.findIssuesWithBookmarks(List.of(awake, dormant)),
			issueJpaRepository.findIssuesWithBookmarksOrderByCreatedDate(List.of(awake, dormant)))) {

			assertThat(rows).hasSize(2);
			for (Object[] row : rows) {
				Long id = ((Number)row[0]).longValue();
				assertThat(row[1]).isEqualTo(id.equals(awake) ? "깨어 있는 이슈" : "잠자는 이슈");
				assertThat(IssueStatus.fromColumn(row[5]))
					.isEqualTo(id.equals(awake) ? IssueStatus.PUBLISHED : IssueStatus.DORMANT);
			}
		}
	}

	@Test
	@DisplayName("목록 응답 변환이 status 를 싣는다")
	void briefResponseCarriesStatus() {
		Long dormant = persist("잠자는 이슈", IssueStatus.DORMANT);

		List<IssueBriefResponse> responses = new IssueProcessorManager()
			.createIssueBriefResponse(issueJpaRepository.findIssuesWithBookmarks(List.of(dormant)));

		assertThat(responses).singleElement()
			.extracting(IssueBriefResponse::getStatus)
			.isEqualTo(IssueStatus.DORMANT);
	}

	@Test
	@DisplayName("상세 쿼리: title 은 [1], status 는 [3]")
	void singleQuery() {
		Long dormant = persist("잠자는 이슈", IssueStatus.DORMANT);

		Object[] row = issueJpaRepository.findSingleIssueWithBookmarks(dormant).get(0);

		assertThat(row[1]).isEqualTo("잠자는 이슈");
		assertThat(IssueStatus.fromColumn(row[3])).isEqualTo(IssueStatus.DORMANT);
	}

	@Test
	@DisplayName("빌더가 status 를 빠뜨려도 null 이 아니라 PUBLISHED 다 (앱 non-null 계약)")
	void briefDefaultIsPublished() {
		assertThat(IssueBriefResponse.builder().issueId(1L).build().getStatus()).isEqualTo(IssueStatus.PUBLISHED);
		assertThat(IssueStatus.fromColumn(null)).isEqualTo(IssueStatus.PUBLISHED);
	}
}
