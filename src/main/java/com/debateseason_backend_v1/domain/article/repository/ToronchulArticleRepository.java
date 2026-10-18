package com.debateseason_backend_v1.domain.article.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.article.entity.ToronchulArticle;

public interface ToronchulArticleRepository extends JpaRepository<ToronchulArticle, Long> {

	Optional<ToronchulArticle> findByArticleKey(String articleKey);
}
