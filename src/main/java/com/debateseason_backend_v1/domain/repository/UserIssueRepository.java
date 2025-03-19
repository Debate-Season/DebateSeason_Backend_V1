package com.debateseason_backend_v1.domain.repository;


import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserIssueRepository extends JpaRepository<UserIssue,Long> {


    UserIssue findByIssueAndUser(Issue issue, User user);

    @Query(value = "SELECT bookmark FROM user_issue WHERE issue_id = :issueId AND user_id = :userId", nativeQuery = true)
    List<Object[]> findByIssueIdAndUserId(@Param("issueId") Long issueId, @Param("userId") Long userId);

}
