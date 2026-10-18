package com.debateseason_backend_v1.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleKeyword;

public interface ToronchulArticleKeywordRepository extends JpaRepository<ToronchulArticleKeyword, Long> {

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("DELETE FROM ToronchulArticleKeyword x WHERE x.articleId = :articleId")
	void deleteByArticleId(@Param("articleId") Long articleId);
}
