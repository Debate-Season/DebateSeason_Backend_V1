package com.debateseason_backend_v1.domain.issue.infrastructure.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

import jakarta.persistence.EntityManager;

/**
 * 이슈맵(GET /api/v1/users/home) 의 정렬을 고정한다.
 *
 * 예전에는 findAll() 이라 정렬이 없었고 PK 순 — 즉 가장 오래된 이슈가 먼저 — 으로 나갔다.
 * 웹도 앱도 클라이언트 정렬을 하지 않고 응답 순서를 그대로 렌더링한다.
 *
 * created_at 만으로는 부족하다. 시드로 한 번에 만든 이슈들이 created_at 이 초 단위까지
 * 같고, 하필 최신 이슈들이 거기 몰려 있다. 2차 키가 없으면 첫 화면이 매 요청 섞인다.
 */
@ActiveProfiles("test")
@DataJpaTest
@DisplayName("이슈맵 정렬")
class IssueMapOrderingTest {

	@Autowired
	private EntityManager em;

	@Autowired
	private IssueJpaRepository issueJpaRepository;

	/**
	 * createdAt 은 @CreatedDate 라 persist 시점에 auditing 이 덮어쓴다. 게다가 auditing 이
	 * 켜지는지 여부가 실행 조합에 따라 달라진다 — AuditingEntityListener 가 정적 BeanFactory 를
	 * 들고 있어서, 같은 JVM 에서 @SpringBootTest 가 먼저 돌면 이 슬라이스 테스트에도 새어든다.
	 * 그래서 저장한 뒤 네이티브 UPDATE 로 값을 확정한다 (updatable=false 라 엔티티 수정으로는 안 된다).
	 */
	private IssueEntity persistIssue(String title, LocalDateTime createdAt) {
		IssueEntity issue = IssueEntity.builder()
			.title(title)
			.majorCategory("정치")
			.build();
		em.persist(issue);
		em.flush();

		em.createNativeQuery("UPDATE issue SET created_at = :ts WHERE issue_id = :id")
			.setParameter("ts", createdAt)
			.setParameter("id", issue.getId())
			.executeUpdate();

		return issue;
	}

	@Test
	@DisplayName("최신 이슈가 먼저 나온다 (PK 순이 아니다)")
	void newestIssueComesFirst() {
		LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
		// 일부러 오래된 것부터 저장한다 — PK 순이면 이 순서 그대로 나온다.
		persistIssue("가장 오래됨", base);
		persistIssue("중간", base.plusDays(10));
		persistIssue("가장 최신", base.plusDays(20));
		em.clear();

		assertThat(issueJpaRepository.findAllByOrderByCreatedAtDescIdDesc())
			.extracting(IssueEntity::getTitle)
			.containsExactly("가장 최신", "중간", "가장 오래됨");
	}

	@Test
	@DisplayName("created_at 이 완전히 같으면 issue_id 내림차순으로 확정된다")
	void tiedCreatedAtIsBrokenByIdDesc() {
		// 운영의 이슈 23~26 처럼 시드로 한 번에 만들어져 시각이 동일한 경우.
		LocalDateTime sameInstant = LocalDateTime.of(2026, 6, 10, 15, 1, 48);
		IssueEntity first = persistIssue("동시 A", sameInstant);
		IssueEntity second = persistIssue("동시 B", sameInstant);
		IssueEntity third = persistIssue("동시 C", sameInstant);
		em.clear();

		assertThat(issueJpaRepository.findAllByOrderByCreatedAtDescIdDesc())
			.extracting(IssueEntity::getId)
			.containsExactly(third.getId(), second.getId(), first.getId());
	}

	@Test
	@DisplayName("시각이 다르면 id 보다 created_at 이 우선한다")
	void createdAtOutranksId() {
		LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
		// 나중에 저장돼 id 가 큰 쪽이 더 오래된 이슈다. created_at 이 이겨야 한다.
		IssueEntity newer = persistIssue("최신", base.plusDays(5));
		IssueEntity olderButHigherId = persistIssue("오래됨 (id 큼)", base);
		em.clear();

		List<IssueEntity> result = issueJpaRepository.findAllByOrderByCreatedAtDescIdDesc();

		assertThat(olderButHigherId.getId()).isGreaterThan(newer.getId());
		assertThat(result).extracting(IssueEntity::getTitle)
			.containsExactly("최신", "오래됨 (id 큼)");
	}
}
