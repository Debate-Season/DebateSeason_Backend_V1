package com.debateseason_backend_v1.domain.article.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleIssue;

public interface ToronchulArticleIssueRepository extends JpaRepository<ToronchulArticleIssue, Long> {

	List<ToronchulArticleIssue> findByArticleId(Long articleId);

	List<ToronchulArticleIssue> findByArticleIdIn(Collection<Long> articleIds);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("DELETE FROM ToronchulArticleIssue x WHERE x.articleId = :articleId")
	void deleteByArticleId(@Param("articleId") Long articleId);

	List<ToronchulArticleIssue> findByIssueId(Long issueId);
}
