package com.debateseason_backend_v1.domain.wiki.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
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
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.wiki.domain.WikiSource;
import com.debateseason_backend_v1.domain.wiki.domain.WikiStatus;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWiki;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWikiRevision;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRepository;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRevisionRepository;
import com.debateseason_backend_v1.domain.wiki.model.response.WikiPublishResponse;
import com.debateseason_backend_v1.domain.wiki.model.response.WikiRevisionResponse;
import com.debateseason_backend_v1.domain.wiki.service.generator.GeneratedWiki;
import com.debateseason_backend_v1.domain.wiki.service.generator.WikiContentGenerator;

class WikiGenerationServiceTest {

	@Mock
	private IssueJpaRepository issueJpaRepository;
	@Mock
	private ChatRoomRepository chatRoomRepository;
	@Mock
	private DebateWikiRepository debateWikiRepository;
	@Mock
	private DebateWikiRevisionRepository debateWikiRevisionRepository;
	@Mock
	private WikiContentGenerator wikiContentGenerator;

	@InjectMocks
	private WikiGenerationService wikiGenerationService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private IssueEntity issue(Long id, String title) {
		return IssueEntity.builder().id(id).title(title).majorCategory("정치").middleCategory("국회").build();
	}

	/** save 시 id 를 부여해 실제 DB 저장을 흉내낸다. */
	private void stubWikiSaveWithId(Long assignedId) {
		when(debateWikiRepository.save(any(DebateWiki.class))).thenAnswer(inv -> {
			DebateWiki w = inv.getArgument(0);
			if (w.getId() == null) {
				w.setId(assignedId);
			}
			return w;
		});
	}

	private void stubRevisionSaveWithId(Long assignedId) {
		when(debateWikiRevisionRepository.save(any(DebateWikiRevision.class))).thenAnswer(inv -> {
			DebateWikiRevision r = inv.getArgument(0);
			r.setId(assignedId);
			return r;
		});
	}

	@Nested
	@DisplayName("초안 생성 (generate)")
	class Generate {

		@Test
		@DisplayName("없던 이슈면 NOT_FOUND_ISSUE 예외, 생성기 호출 안 함")
		void notFoundIssue() {
			when(issueJpaRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> wikiGenerationService.generate(99L))
				.isInstanceOf(CustomException.class);

			verifyNoInteractions(wikiContentGenerator);
			verify(debateWikiRevisionRepository, never()).save(any());
		}

		@Test
		@DisplayName("생성기가 실패(미구성)하면 위키/리비전을 저장하지 않는다")
		void generatorUnavailable() {
			IssueEntity issue = issue(6L, "이슈");
			when(issueJpaRepository.findById(6L)).thenReturn(Optional.of(issue));
			when(chatRoomRepository.findThreadRoomIdsByIssueId(6L)).thenReturn(List.of());
			when(chatRoomRepository.findAllById(anyIterable())).thenReturn(List.of());
			when(wikiContentGenerator.generate(any())).thenThrow(new CustomException(
				com.debateseason_backend_v1.common.exception.ErrorCode.WIKI_GENERATION_UNAVAILABLE));

			assertThatThrownBy(() -> wikiGenerationService.generate(6L))
				.isInstanceOf(CustomException.class);

			verify(debateWikiRepository, never()).save(any());
			verify(debateWikiRevisionRepository, never()).save(any());
		}

		@Test
		@DisplayName("초안 생성 시 새 위키(DRAFT) + AI 리비전을 저장한다")
		void generatesDraft() {
			IssueEntity issue = issue(6L, "이슈 제목");
			when(issueJpaRepository.findById(6L)).thenReturn(Optional.of(issue));
			when(chatRoomRepository.findThreadRoomIdsByIssueId(6L)).thenReturn(List.of());
			when(chatRoomRepository.findAllById(anyIterable())).thenReturn(List.of());
			when(wikiContentGenerator.generate(any()))
				.thenReturn(new GeneratedWiki("## 개요\n본문", "claude-opus-4-8"));
			when(debateWikiRepository.findByIssueId(6L)).thenReturn(Optional.empty());
			when(debateWikiRevisionRepository.findByWikiIdOrderByIdDesc(3L)).thenReturn(List.of());
			stubWikiSaveWithId(3L);
			stubRevisionSaveWithId(12L);

			WikiRevisionResponse res = wikiGenerationService.generate(6L);

			assertThat(res.getWikiId()).isEqualTo(3L);
			assertThat(res.getRevisionId()).isEqualTo(12L);
			assertThat(res.getIssueId()).isEqualTo(6L);
			assertThat(res.getStatus()).isEqualTo("DRAFT");
			assertThat(res.getSource()).isEqualTo("AI");
			assertThat(res.getModel()).isEqualTo("claude-opus-4-8");
			assertThat(res.getPreview()).isEqualTo("## 개요\n본문");

			// 저장된 리비전이 AI 생성·최초 생성 요약을 갖는지 확인
			org.mockito.ArgumentCaptor<DebateWikiRevision> captor =
				org.mockito.ArgumentCaptor.forClass(DebateWikiRevision.class);
			verify(debateWikiRevisionRepository).save(captor.capture());
			assertThat(captor.getValue().getSource()).isEqualTo(WikiSource.AI);
			assertThat(captor.getValue().getEditSummary()).isEqualTo("최초 AI 생성");
		}

		@Test
		@DisplayName("기존 리비전이 있으면 요약이 'AI 재생성'")
		void regeneration() {
			IssueEntity issue = issue(6L, "이슈");
			DebateWiki existing = DebateWiki.builder().id(3L).issueId(6L).status(WikiStatus.DRAFT).build();
			DebateWikiRevision prev = DebateWikiRevision.builder().id(11L).wikiId(3L).build();

			when(issueJpaRepository.findById(6L)).thenReturn(Optional.of(issue));
			when(chatRoomRepository.findThreadRoomIdsByIssueId(6L)).thenReturn(List.of());
			when(chatRoomRepository.findAllById(anyIterable())).thenReturn(List.of());
			when(wikiContentGenerator.generate(any()))
				.thenReturn(new GeneratedWiki("본문2", "claude-opus-4-8"));
			when(debateWikiRepository.findByIssueId(6L)).thenReturn(Optional.of(existing));
			when(debateWikiRevisionRepository.findByWikiIdOrderByIdDesc(3L)).thenReturn(List.of(prev));
			stubRevisionSaveWithId(12L);

			wikiGenerationService.generate(6L);

			org.mockito.ArgumentCaptor<DebateWikiRevision> captor =
				org.mockito.ArgumentCaptor.forClass(DebateWikiRevision.class);
			verify(debateWikiRevisionRepository).save(captor.capture());
			assertThat(captor.getValue().getEditSummary()).isEqualTo("AI 재생성");
			// 기존 위키가 있으면 새로 저장하지 않는다
			verify(debateWikiRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("게시 (publish)")
	class Publish {

		@Test
		@DisplayName("리비전을 PUBLISHED 로 전환하고 publishedRevisionId 를 세팅한다")
		void publishes() {
			DebateWikiRevision revision = DebateWikiRevision.builder().id(12L).wikiId(3L).build();
			DebateWiki wiki = DebateWiki.builder().id(3L).issueId(6L).status(WikiStatus.DRAFT).build();

			when(debateWikiRevisionRepository.findById(12L)).thenReturn(Optional.of(revision));
			when(debateWikiRepository.findById(3L)).thenReturn(Optional.of(wiki));
			when(debateWikiRepository.save(any(DebateWiki.class))).thenAnswer(inv -> inv.getArgument(0));

			WikiPublishResponse res = wikiGenerationService.publish(12L);

			assertThat(res.getStatus()).isEqualTo("PUBLISHED");
			assertThat(res.getPublishedRevisionId()).isEqualTo(12L);
			assertThat(res.getIssueId()).isEqualTo(6L);
			assertThat(wiki.getStatus()).isEqualTo(WikiStatus.PUBLISHED);
			assertThat(wiki.getPublishedRevisionId()).isEqualTo(12L);
		}

		@Test
		@DisplayName("없는 리비전이면 NOT_FOUND_WIKI_REVISION 예외")
		void revisionNotFound() {
			when(debateWikiRevisionRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> wikiGenerationService.publish(99L))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("수동 편집 (addManualRevision)")
	class ManualRevision {

		@Test
		@DisplayName("ADMIN 리비전을 createdBy 와 함께 저장한다")
		void addsAdminRevision() {
			IssueEntity issue = issue(6L, "이슈");
			DebateWiki wiki = DebateWiki.builder().id(3L).issueId(6L).status(WikiStatus.DRAFT).build();

			when(issueJpaRepository.findById(6L)).thenReturn(Optional.of(issue));
			when(debateWikiRepository.findByIssueId(6L)).thenReturn(Optional.of(wiki));
			stubRevisionSaveWithId(20L);

			WikiRevisionResponse res =
				wikiGenerationService.addManualRevision(6L, "수정된 본문", "오탈자 수정", 7L);

			assertThat(res.getRevisionId()).isEqualTo(20L);
			assertThat(res.getSource()).isEqualTo("ADMIN");
			assertThat(res.getModel()).isNull();

			org.mockito.ArgumentCaptor<DebateWikiRevision> captor =
				org.mockito.ArgumentCaptor.forClass(DebateWikiRevision.class);
			verify(debateWikiRevisionRepository).save(captor.capture());
			assertThat(captor.getValue().getSource()).isEqualTo(WikiSource.ADMIN);
			assertThat(captor.getValue().getCreatedBy()).isEqualTo(7L);
			assertThat(captor.getValue().getEditSummary()).isEqualTo("오탈자 수정");
		}
	}
}
