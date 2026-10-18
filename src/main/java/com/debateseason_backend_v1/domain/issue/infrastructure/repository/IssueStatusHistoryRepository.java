package com.debateseason_backend_v1.domain.issue.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueStatusHistory;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Long> {
}
