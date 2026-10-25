package com.debateseason_backend_v1.domain.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.article.entity.ToronchulArticle;

public interface ToronchulArticleRepository extends JpaRepository<ToronchulArticle, Long> {

	Optional<ToronchulArticle> findByArticleKey(String articleKey);

	// 게시 최신순 커서 페이지. (date_published, id) 내림차순으로 같은 시각에 여러 편이어도 순서가 확정된다.
	@Query("""
		SELECT a FROM ToronchulArticle a
		WHERE a.edition = :edition AND a.status = com.debateseason_backend_v1.domain.article.entity.ArticleStatus.PUBLISHED
		  AND (:cursorAt IS NULL OR a.datePublished < :cursorAt OR (a.datePublished = :cursorAt AND a.id < :cursorId))
		ORDER BY a.datePublished DESC, a.id DESC
		""")
	List<ToronchulArticle> findPublishedPage(@Param("edition") String edition, @Param("cursorAt") LocalDateTime cursorAt,
		@Param("cursorId") long cursorId, Pageable pageable);
}
