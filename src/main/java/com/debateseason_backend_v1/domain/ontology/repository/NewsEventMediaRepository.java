package com.debateseason_backend_v1.domain.ontology.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.ontology.entity.NewsEventMedia;

public interface NewsEventMediaRepository extends JpaRepository<NewsEventMedia, Long> {

	List<NewsEventMedia> findByEventId(Long eventId);

	List<NewsEventMedia> findByEventIdAndMediaIdIn(Long eventId, Collection<Long> mediaIds);
}
