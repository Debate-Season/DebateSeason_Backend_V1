package com.debateseason_backend_v1.domain.issue.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueStatusHistory;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueStatusHistoryRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.issue.model.response.IssueStatusChangeResponse;

@DisplayName("이슈 잠재우기·깨우기")
class IssueStatusServiceTest {

	@Mock
	private IssueJpaRepository issueJpaRepository;
	@Mock
	private IssueStatusHistoryRepository issueStatusHistoryRepository;

	@InjectMocks
	private IssueStatusService issueStatusService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private IssueEntity issueWith(IssueStatus status) {
		IssueEntity issue = IssueEntity.builder().id(6L).title("이슈").status(status).build();
		when(issueJpaRepository.findById(6L)).thenReturn(Optional.of(issue));
		return issue;
	}

	@Test
	@DisplayName("PUBLISHED -> DORMANT: 상태를 바꾸고 요청 주체·승인자를 이력에 남긴다")
	void sleep() {
		IssueEntity issue = issueWith(IssueStatus.PUBLISHED);

		IssueStatusChangeResponse response = issueStatusService.changeStatus(
			6L, IssueStatus.DORMANT, "14일간 새 사건 없음", "123456789", "service:ontology");

		assertThat(issue.getStatus()).isEqualTo(IssueStatus.DORMANT);
		assertThat(response.changed()).isTrue();
		assertThat(response.fromStatus()).isEqualTo(IssueStatus.PUBLISHED);

		ArgumentCaptor<IssueStatusHistory> captor = ArgumentCaptor.forClass(IssueStatusHistory.class);
		verify(issueStatusHistoryRepository).save(captor.capture());
		IssueStatusHistory history = captor.getValue();
		assertThat(history.getIssueId()).isEqualTo(6L);
		assertThat(history.getFromStatus()).isEqualTo(IssueStatus.PUBLISHED);
		assertThat(history.getToStatus()).isEqualTo(IssueStatus.DORMANT);
		assertThat(history.getReason()).isEqualTo("14일간 새 사건 없음");
		assertThat(history.getDecidedBy()).isEqualTo("service:ontology");
		assertThat(history.getApprover()).isEqualTo("123456789");
		assertThat(history.getDecidedAt()).isNotNull();
	}

	@Test
	@DisplayName("DORMANT -> PUBLISHED: 깨우기도 같은 경로다")
	void wake() {
		IssueEntity issue = issueWith(IssueStatus.DORMANT);

		issueStatusService.changeStatus(6L, IssueStatus.PUBLISHED, "새 사건 발생", "123456789", "service:ontology");

		assertThat(issue.getStatus()).isEqualTo(IssueStatus.PUBLISHED);
		verify(issueStatusHistoryRepository).save(any());
	}

	@Test
	@DisplayName("이미 그 상태면 changed=false, 이력은 남기지 않는다 — 파이프라인 재시도 대비")
	void idempotent() {
		issueWith(IssueStatus.DORMANT);

		IssueStatusChangeResponse response = issueStatusService.changeStatus(
			6L, IssueStatus.DORMANT, "재시도", "123456789", "service:ontology");

		assertThat(response.changed()).isFalse();
		verify(issueStatusHistoryRepository, never()).save(any());
	}

	@ParameterizedTest(name = "{0} -> {1} 는 409")
	@CsvSource({
		"DORMANT, DRAFT",
		"ARCHIVED, DRAFT",
		"DRAFT, PUBLISHED",
		"DRAFT, DORMANT"
	})
	@DisplayName("DRAFT 로는 공개 롤백(PUBLISHED→DRAFT)만, DRAFT 에서는 ARCHIVED 로만 — 공개는 launch 로만")
	void rejectsOtherTransitions(IssueStatus from, IssueStatus to) {
		IssueEntity issue = issueWith(from);

		assertThatThrownBy(() -> issueStatusService.changeStatus(6L, to, null, null, "service:ontology"))
			.isInstanceOf(CustomException.class)
			.extracting("codeInterface")
			.isEqualTo(from == IssueStatus.DRAFT ? ErrorCode.ISSUE_LAUNCH_REQUIRED : ErrorCode.INVALID_ISSUE_STATUS_TRANSITION);

		assertThat(issue.getStatus()).isEqualTo(from);
		verify(issueStatusHistoryRepository, never()).save(any());
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"PUBLISHED, ARCHIVED",
		"DORMANT, ARCHIVED",
		"ARCHIVED, PUBLISHED",
		"ARCHIVED, DORMANT",
		"PUBLISHED, DRAFT",
		"DRAFT, ARCHIVED"
	})
	@DisplayName("신설 롤백(→ ARCHIVED)과 그 되돌리기는 허용된다")
	void archiveRoundTrip(IssueStatus from, IssueStatus to) {
		IssueEntity issue = issueWith(from);

		issueStatusService.changeStatus(6L, to, "롤백", "123456789", "service:ontology");

		assertThat(issue.getStatus()).isEqualTo(to);
		verify(issueStatusHistoryRepository).save(any());
	}

	@Test
	@DisplayName("없는 이슈는 404")
	void notFound() {
		when(issueJpaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> issueStatusService.changeStatus(99L, IssueStatus.DORMANT, null, null, "user:1"))
			.isInstanceOf(CustomException.class)
			.extracting("codeInterface")
			.isEqualTo(ErrorCode.NOT_FOUND_ISSUE);
	}
}
