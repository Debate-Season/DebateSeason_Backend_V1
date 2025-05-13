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

    // 사용자 A가 이슈 "some Issue"에 북마크를 했는지 여부를 반환받을 수 있다. yes or no
    @Query(value = "SELECT bookmark FROM user_issue WHERE issue_id = :issueId AND user_id = :userId", nativeQuery = true)
    String findBookMarkByIssueIdAndUserId(@Param("issueId") Long issueId, @Param("userId") Long userId);

}
