package com.debateseason_backend_v1.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleRevision;

public interface ToronchulArticleRevisionRepository extends JpaRepository<ToronchulArticleRevision, Long> {
}
