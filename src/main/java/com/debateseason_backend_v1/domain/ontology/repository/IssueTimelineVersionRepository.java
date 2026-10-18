package com.debateseason_backend_v1.domain.ontology.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.ontology.entity.IssueTimelineVersion;

public interface IssueTimelineVersionRepository extends JpaRepository<IssueTimelineVersion, Long> {

	Optional<IssueTimelineVersion> findFirstByIssueIdOrderByVersionDesc(Long issueId);

	List<IssueTimelineVersion> findByIssueIdOrderByVersionDesc(Long issueId);
}
