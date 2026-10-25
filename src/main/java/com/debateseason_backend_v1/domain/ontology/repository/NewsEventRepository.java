package com.debateseason_backend_v1.domain.ontology.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.issue.model.IssueVisibility;

import com.debateseason_backend_v1.domain.ontology.entity.NewsEvent;

public interface NewsEventRepository extends JpaRepository<NewsEvent, Long> {

	// 목록에 나오는 이슈에 걸린 사건, 생성 최신순. 병합으로 흡수된 사건은 뺀다 (웹 홈 '사건 발생!').
	@Query(value = "SELECT e.id FROM news_event e WHERE e.merged_into IS NULL"
		+ " AND EXISTS (SELECT 1 FROM issue_news_event ine JOIN issue i ON i.issue_id = ine.issue_id"
		+ " WHERE ine.event_id = e.id AND" + IssueVisibility.LISTED_I + ")"
		+ " ORDER BY e.created_at DESC, e.id DESC LIMIT :size", nativeQuery = true)
	List<Long> findRecentListedIds(@Param("size") int size);

	// 사건별 목록에 나오는 이슈. 행 = [event_id, issue_id]
	@Query(value = "SELECT ine.event_id, ine.issue_id FROM issue_news_event ine JOIN issue i ON i.issue_id = ine.issue_id"
		+ " WHERE ine.event_id IN (:eventIds) AND" + IssueVisibility.LISTED_I + "ORDER BY ine.issue_id", nativeQuery = true)
	List<Object[]> findListedIssuePairs(@Param("eventIds") Collection<Long> eventIds);
}
