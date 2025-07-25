package com.debateseason_backend_v1.domain.issue.infrastructure.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.issue.application.repository.IssueRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class IssueRepositoryImpl implements IssueRepository {

	private final IssueJpaRepository issueJpaRepository;

	// 기본적인 CRUD
	@Override
	public void save(IssueEntity issueEntity) {

		try {
			issueJpaRepository.save(issueEntity);
		}
		catch (Exception e){
			log.info("IssueSericeV1.save -> issueRepository.save에서 발생한 오류 : issue를 저장하지 못함.");
			throw new RuntimeException(e);
		}

	}
	@Override
	public IssueEntity findById(Long issueId) {
		return issueJpaRepository.findById(issueId).orElseThrow(
			()-> new CustomException(ErrorCode.NOT_FOUND_ISSUE)
		)
		;
	}

	//
	@Override
	public List<Long> findIssuesByPage(Long page) {
		return issueJpaRepository.findIssuesByPage(page);
	}

	@Override
	public List<Long> findTop6IssuesByPage(Long page) {
		return issueJpaRepository.findTop6IssuesByPage(page);
	}

	@Override
	public List<Long> findTop6Issues() {
		return issueJpaRepository.findTop6Issues();
	}

	// 1. 첫 조회시, 이슈 가져옴 + 카테고리 필터링
	@Override
	public List<Long> findTop6IssuesByCategory(String majorCategory) {
		return issueJpaRepository.findTop6IssuesByCategory(majorCategory);
	}

	//1-2. 이슈 가져옴 + 카테고리 필터링 + 페이지네이션
	@Override
	public List<Long> findTop6IssuesByPageAndCategory(String majorCategory, Long page) {
		return issueJpaRepository.findTop6IssuesByPageAndCategory(majorCategory,page);
	}

	@Override
	public List<Object[]> findIssuesWithBookmarks(List<Long> issueIds) {
		return issueJpaRepository.findIssuesWithBookmarks(issueIds);
	}

	@Override
	public List<Object[]> findIssuesWithBookmarksOrderByCreatedDate(List<Long> issueIds) {
		return issueJpaRepository.findIssuesWithBookmarksOrderByCreatedDate(issueIds);
	}

	@Override
	public List<Object[]> findSingleIssueWithBookmarks(Long issueId) {
		return issueJpaRepository.findSingleIssueWithBookmarks(issueId);
	}

	@Override
	public List<Object[]> findTop5ActiveIssuesByCountingChats() {
		return issueJpaRepository.findTop5ActiveIssuesByCountingChats();
	}

	@Override
	public Long countChatsTodayByIssueId(Long issueId) {
		return issueJpaRepository.countChatsTodayByIssueId(issueId);
	}
}
