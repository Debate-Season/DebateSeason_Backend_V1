package com.debateseason_backend_v1.domain.wiki.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
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
import com.debateseason_backend_v1.domain.wiki.service.generator.WikiGenerationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * v1.4.0 — 토론위키 생성·검수 (Phase 2, ADMIN 전용 쓰기).
 *
 * <p>세 가지 ADMIN 오퍼레이션:
 * <ul>
 *   <li>{@link #generate} — 원천 재료 조립 → LLM 생성 → <b>DRAFT 리비전</b> 저장(게시 안 함)</li>
 *   <li>{@link #publish} — 특정 리비전을 PUBLISHED 로 전환(게시 상태의 단일 진실)</li>
 *   <li>{@link #addManualRevision} — ADMIN 수동 편집 리비전 추가</li>
 * </ul>
 *
 * <p>서빙(읽기)은 {@link WikiServiceV2} 가 게시본만 싸게 내보낸다. 실제 LLM 호출은
 * {@link WikiContentGenerator} 뒤에 숨어 있고, 키 확보 전에는 스텁이 503 을 던진다.
 * <b>자동 게시는 없다</b> — 생성은 항상 DRAFT, ADMIN 검수 후 publish (정치·시사 중립성).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WikiGenerationService {

	private static final int PREVIEW_MAX_LENGTH = 500;

	private final IssueJpaRepository issueJpaRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final DebateWikiRepository debateWikiRepository;
	private final DebateWikiRevisionRepository debateWikiRevisionRepository;
	private final WikiContentGenerator wikiContentGenerator;

	/**
	 * 이슈의 원천 재료로 위키 초안을 생성해 DRAFT 리비전으로 저장한다. 게시 상태는 바꾸지 않는다.
	 */
	@Transactional
	public WikiRevisionResponse generate(Long issueId) {

		IssueEntity issue = issueJpaRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));

		// 1. 원천 재료 조립 (이슈 메타 + 스레드 제목=쟁점)
		WikiGenerationContext context = assembleContext(issue);

		// 2. LLM 생성 (미구성이면 여기서 503 — 아래 저장 부수효과 없음)
		GeneratedWiki generated = wikiContentGenerator.generate(context);

		// 3. 위키 메타 upsert (이슈당 1장) 후 DRAFT 리비전 저장
		DebateWiki wiki = upsertWiki(issueId);
		boolean isRegeneration = !debateWikiRevisionRepository.findByWikiIdOrderByIdDesc(wiki.getId()).isEmpty();

		DebateWikiRevision revision = debateWikiRevisionRepository.save(
			DebateWikiRevision.builder()
				.wikiId(wiki.getId())
				.content(generated.content())
				.source(WikiSource.AI)
				.model(generated.model())
				.editSummary(isRegeneration ? "AI 재생성" : "최초 AI 생성")
				.build()
		);

		log.info("위키 DRAFT 리비전 생성: issueId={}, wikiId={}, revisionId={}, model={}",
			issueId, wiki.getId(), revision.getId(), generated.model());

		return toRevisionResponse(wiki, issue.getId(), revision);
	}

	/**
	 * 특정 리비전을 게시한다. 롤백도 옛 리비전 id 로 다시 publish 하면 된다.
	 */
	@Transactional
	public WikiPublishResponse publish(Long revisionId) {

		DebateWikiRevision revision = debateWikiRevisionRepository.findById(revisionId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_WIKI_REVISION));

		DebateWiki wiki = debateWikiRepository.findById(revision.getWikiId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_WIKI));

		wiki.setStatus(WikiStatus.PUBLISHED);
		wiki.setPublishedRevisionId(revisionId);
		debateWikiRepository.save(wiki);

		log.info("위키 게시: wikiId={}, publishedRevisionId={}", wiki.getId(), revisionId);

		return WikiPublishResponse.builder()
			.wikiId(wiki.getId())
			.issueId(wiki.getIssueId())
			.status(wiki.getStatus().name())
			.publishedRevisionId(wiki.getPublishedRevisionId())
			.build();
	}

	/**
	 * ADMIN 수동 편집 리비전을 추가한다({@code source=ADMIN}). 게시 상태는 바꾸지 않는다.
	 */
	@Transactional
	public WikiRevisionResponse addManualRevision(Long issueId, String content, String editSummary, Long adminUserId) {

		IssueEntity issue = issueJpaRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));

		DebateWiki wiki = upsertWiki(issueId);

		DebateWikiRevision revision = debateWikiRevisionRepository.save(
			DebateWikiRevision.builder()
				.wikiId(wiki.getId())
				.content(content)
				.source(WikiSource.ADMIN)
				.model(null)
				.createdBy(adminUserId)
				.editSummary(editSummary)
				.build()
		);

		log.info("위키 ADMIN 수동 리비전 추가: issueId={}, wikiId={}, revisionId={}, adminUserId={}",
			issueId, wiki.getId(), revision.getId(), adminUserId);

		return toRevisionResponse(wiki, issue.getId(), revision);
	}

	// --- 내부 헬퍼 ---

	private WikiGenerationContext assembleContext(IssueEntity issue) {

		List<Long> threadIds = chatRoomRepository.findThreadRoomIdsByIssueId(issue.getId());
		List<String> threadTitles = chatRoomRepository.findAllById(threadIds).stream()
			.map(ChatRoom::getTitle)
			.filter(title -> title != null && !title.isBlank())
			.distinct()
			.toList();

		return new WikiGenerationContext(
			issue.getId(),
			issue.getTitle(),
			issue.getMajorCategory(),
			issue.getMiddleCategory(),
			threadTitles
		);
	}

	private DebateWiki upsertWiki(Long issueId) {
		return debateWikiRepository.findByIssueId(issueId)
			.orElseGet(() -> debateWikiRepository.save(
				DebateWiki.builder()
					.issueId(issueId)
					.status(WikiStatus.DRAFT)
					.build()
			));
	}

	private WikiRevisionResponse toRevisionResponse(DebateWiki wiki, Long issueId, DebateWikiRevision revision) {
		return WikiRevisionResponse.builder()
			.wikiId(wiki.getId())
			.revisionId(revision.getId())
			.issueId(issueId)
			.status(wiki.getStatus().name())
			.source(revision.getSource().name())
			.model(revision.getModel())
			.preview(preview(revision.getContent()))
			.build();
	}

	private String preview(String content) {
		if (content == null) {
			return null;
		}
		return content.length() <= PREVIEW_MAX_LENGTH ? content : content.substring(0, PREVIEW_MAX_LENGTH);
	}
}
