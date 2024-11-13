package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.issue.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue,Long> {
    Issue findByTitle(String title);
}
