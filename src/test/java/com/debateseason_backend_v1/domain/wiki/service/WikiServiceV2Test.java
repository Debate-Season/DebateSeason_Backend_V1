package com.debateseason_backend_v1.domain.wiki.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.wiki.domain.WikiSource;
import com.debateseason_backend_v1.domain.wiki.domain.WikiStatus;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWiki;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWikiRevision;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRepository;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRevisionRepository;
import com.debateseason_backend_v1.domain.wiki.model.response.WikiResponse;

class WikiServiceV2Test {

	@Mock
	private DebateWikiRepository debateWikiRepository;
	@Mock
	private DebateWikiRevisionRepository debateWikiRevisionRepository;
	@Mock
	private IssueJpaRepository issueJpaRepository;

	@InjectMocks
	private WikiServiceV2 wikiServiceV2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private IssueEntity issue(Long id, String title) {
		return IssueEntity.builder().id(id).title(title).build();
	}

	@Nested
	@DisplayName("게시된 위키 조회")
	class GetPublishedWiki {

		@Test
		@DisplayName("존재하지 않는 이슈면 NOT_FOUND_ISSUE 예외")
		void notFoundIssue() {
			when(issueJpaRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> wikiServiceV2.getPublishedWiki(99L))
				.isInstanceOf(CustomException.class);
		}

		@Test
		@DisplayName("게시된 위키는 본문·모델·리비전을 채워 반환")
		void published() {
			IssueEntity issue = issue(6L, "이슈 제목");
			DebateWiki wiki = DebateWiki.builder()
				.id(1L).issueId(6L).status(WikiStatus.PUBLISHED).publishedRevisionId(12L)
				.build();
			DebateWikiRevision revision = DebateWikiRevision.builder()
				.id(12L).wikiId(1L).content("## 개요\n본문").source(WikiSource.AI).model("claude-opus-4-8")
				.build();

			when(issueJpaRepository.findById(6L)).thenReturn(Optional.of(issue));
			when(debateWikiRepository.findByIssueId(6L)).thenReturn(Optional.of(wiki));
			when(debateWikiRevisionRepository.findById(12L)).thenReturn(Optional.of(revision));

			WikiResponse res = wikiServiceV2.getPublishedWiki(6L);

			assertThat(res.getIssueId()).isEqualTo(6L);
			assertThat(res.getIssueTitle()).isEqualTo("이슈 제목");
			assertThat(res.getStatus()).isEqualTo("PUBLISHED");
			assertThat(res.getContent()).isEqualTo("## 개요\n본문");
			assertThat(res.getModel()).isEqualTo("claude-opus-4-8");
			assertThat(res.getRevisionId()).isEqualTo(12L);
		}

		@Test
		@DisplayName("위키가 없으면 status/content 는 null (에러 아님)")
		void noWiki() {
			IssueEntity issue = issue(7L, "위키 없는 이슈");
			when(issueJpaRepository.findById(7L)).thenReturn(Optional.of(issue));
			when(debateWikiRepository.findByIssueId(7L)).thenReturn(Optional.empty());

			WikiResponse res = wikiServiceV2.getPublishedWiki(7L);

			assertThat(res.getIssueId()).isEqualTo(7L);
			assertThat(res.getStatus()).isNull();
			assertThat(res.getContent()).isNull();
			verify(debateWikiRevisionRepository, never()).findById(any());
		}

		@Test
		@DisplayName("DRAFT 상태(미게시)면 본문을 내리지 않는다")
		void draftNotServed() {
			IssueEntity issue = issue(8L, "초안만 있는 이슈");
			DebateWiki wiki = DebateWiki.builder()
				.id(2L).issueId(8L).status(WikiStatus.DRAFT).publishedRevisionId(null)
				.build();

			when(issueJpaRepository.findById(8L)).thenReturn(Optional.of(issue));
			when(debateWikiRepository.findByIssueId(8L)).thenReturn(Optional.of(wiki));

			WikiResponse res = wikiServiceV2.getPublishedWiki(8L);

			assertThat(res.getStatus()).isNull();
			assertThat(res.getContent()).isNull();
			verify(debateWikiRevisionRepository, never()).findById(any());
		}
	}
}
