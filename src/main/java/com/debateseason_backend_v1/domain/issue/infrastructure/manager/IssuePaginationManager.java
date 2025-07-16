package com.debateseason_backend_v1.domain.issue.infrastructure.manager;

import java.util.List;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.issue.application.repository.IssueRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IssuePaginationManager {

	private final IssueRepository issueRepository;

	// 1. 이슈 가져오는 메소드. 단순 select인데 굳이 @Transaction로 관리해야하나???
	public List<Long> getIssue(String majorCategory,Long page){ // 대분류, 중분류, 소분류로 가져오기 + 페이지네이션

		List<Long> issueIds;

		if (majorCategory == null) {

			// 전체 불러오기 + 페이지 네이션
			if (page == null) {
				//issueIds = issueJpaRepository.findTop6Issues();
				issueIds = issueRepository.findTop6Issues();
			} else {
				//issueIds = issueJpaRepository.findTop6IssuesByPage(page);
				issueIds = issueRepository.findTop6IssuesByPage(page);
			}

		} else {
			// 카테고리 필터링 적용하기 + 페이지 네이션
			if (page == null) {
				//issueIds = issueJpaRepository.findTop6IssuesByCategory(majorCategory);

				issueIds = issueRepository.findTop6IssuesByCategory(majorCategory);
			} else {
				//issueIds = issueJpaRepository.findTop6IssuesByPageAndCategory(majorCategory, page);
				issueIds = issueRepository.findTop6IssuesByPageAndCategory(majorCategory, page);
			}
		}



		if (issueIds.isEmpty()) {
			// 페이지네이션 오류
			throw new CustomException(ErrorCode.PAGE_OUT_OF_RANGE);
		}

		return issueIds;
	}
}
