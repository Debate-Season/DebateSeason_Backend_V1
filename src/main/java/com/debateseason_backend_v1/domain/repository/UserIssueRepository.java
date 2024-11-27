package com.debateseason_backend_v1.domain.repository;


import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserIssueRepository extends JpaRepository<UserIssue,Long> {
    // 1. User로 UserIssue(중간테이블) 조회하기
    UserIssue findByUser(User user);
    // 2. Issue로 UserIssue(중간테이블) 조회하기
    List<UserIssue> findByIssue(Issue issue);

}
