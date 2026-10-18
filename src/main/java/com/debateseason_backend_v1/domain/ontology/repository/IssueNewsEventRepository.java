package com.debateseason_backend_v1.domain.ontology.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.ontology.entity.IssueNewsEvent;

public interface IssueNewsEventRepository extends JpaRepository<IssueNewsEvent, Long> {

	List<IssueNewsEvent> findByIssueIdAndEventIdIn(Long issueId, Collection<Long> eventIds);
}
