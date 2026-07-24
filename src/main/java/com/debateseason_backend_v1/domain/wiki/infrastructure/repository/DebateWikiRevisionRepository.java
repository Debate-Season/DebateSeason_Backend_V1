package com.debateseason_backend_v1.domain.wiki.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWikiRevision;

@Repository
public interface DebateWikiRevisionRepository extends JpaRepository<DebateWikiRevision, Long> {

	// 위키의 리비전 이력 (최신순). Phase 3 이력 API 용.
	List<DebateWikiRevision> findByWikiIdOrderByIdDesc(Long wikiId);
}
