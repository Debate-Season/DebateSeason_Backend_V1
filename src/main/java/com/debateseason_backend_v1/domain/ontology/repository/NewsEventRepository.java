package com.debateseason_backend_v1.domain.ontology.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.ontology.entity.NewsEvent;

public interface NewsEventRepository extends JpaRepository<NewsEvent, Long> {
}
