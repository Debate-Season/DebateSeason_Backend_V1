package com.debateseason_backend_v1.domain.ontology.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.ontology.entity.IssueNewsEvent;

public interface IssueNewsEventRepository extends JpaRepository<IssueNewsEvent, Long> {

	List<IssueNewsEvent> findByIssueIdAndEventIdIn(Long issueId, Collection<Long> eventIds);

	Optional<IssueNewsEvent> findByIssueIdAndEventId(Long issueId, Long eventId);

	List<IssueNewsEvent> findByIssueId(Long issueId);

	List<IssueNewsEvent> findByEventId(Long eventId);
}
