package com.debateseason_backend_v1.domain.article.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleEvent;

public interface ToronchulArticleEventRepository extends JpaRepository<ToronchulArticleEvent, Long> {

	List<ToronchulArticleEvent> findByArticleId(Long articleId);

	List<ToronchulArticleEvent> findByArticleIdIn(Collection<Long> articleIds);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("DELETE FROM ToronchulArticleEvent x WHERE x.articleId = :articleId")
	void deleteByArticleId(@Param("articleId") Long articleId);

	List<ToronchulArticleEvent> findByNewsEventIdIn(Collection<Long> newsEventIds);
}
