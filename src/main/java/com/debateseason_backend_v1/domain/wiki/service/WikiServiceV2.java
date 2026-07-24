package com.debateseason_backend_v1.domain.wiki.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.wiki.domain.WikiStatus;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWiki;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWikiRevision;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRepository;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRevisionRepository;
import com.debateseason_backend_v1.domain.wiki.model.response.WikiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * v1.4.0 — 토론위키 서빙 (Phase 1, 읽기 전용).
 *
 * 생성(LLM 호출)은 Phase 2 의 ADMIN 경로에서 하고, 여기서는 게시된(PUBLISHED) 본문만 싸게 내보낸다.
 * 요청마다 LLM 을 부르지 않는다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WikiServiceV2 {

	private final DebateWikiRepository debateWikiRepository;
	private final DebateWikiRevisionRepository debateWikiRevisionRepository;
	private final IssueJpaRepository issueJpaRepository;

	public WikiResponse getPublishedWiki(Long issueId) {

		// 1. 이슈 확인
		IssueEntity issue = issueJpaRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));

		// 2. 위키 메타 (없거나 미게시면 본문 없이 반환 — 에러 아님)
		Optional<DebateWiki> wikiOpt = debateWikiRepository.findByIssueId(issueId);
		if (wikiOpt.isEmpty()) {
			return emptyResponse(issue);
		}

		DebateWiki wiki = wikiOpt.get();
		if (wiki.getStatus() != WikiStatus.PUBLISHED || wiki.getPublishedRevisionId() == null) {
			return emptyResponse(issue);
		}

		// 3. 게시 리비전 본문
		//    published_revision_id 가 가리키는 리비전이 (데이터 이상으로) 없으면 본문 없이 반환.
		Optional<DebateWikiRevision> revisionOpt =
			debateWikiRevisionRepository.findById(wiki.getPublishedRevisionId());
		if (revisionOpt.isEmpty()) {
			log.warn("게시 리비전 누락: wikiId={}, publishedRevisionId={}", wiki.getId(), wiki.getPublishedRevisionId());
			return emptyResponse(issue);
		}

		DebateWikiRevision revision = revisionOpt.get();
		return WikiResponse.builder()
			.issueId(issue.getId())
			.issueTitle(issue.getTitle())
			.status(wiki.getStatus().name())
			.content(revision.getContent())
			.model(revision.getModel())
			.revisionId(revision.getId())
			.updatedAt(wiki.getUpdatedAt())
			.build();
	}

	private WikiResponse emptyResponse(IssueEntity issue) {
		return WikiResponse.builder()
			.issueId(issue.getId())
			.issueTitle(issue.getTitle())
			.status(null)
			.content(null)
			.build();
	}
}
