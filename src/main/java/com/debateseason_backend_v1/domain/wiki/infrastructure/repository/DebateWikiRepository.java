package com.debateseason_backend_v1.domain.wiki.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWiki;

@Repository
public interface DebateWikiRepository extends JpaRepository<DebateWiki, Long> {

	// 이슈 1:1 — 이슈로 위키 메타 조회.
	Optional<DebateWiki> findByIssueId(Long issueId);
}
