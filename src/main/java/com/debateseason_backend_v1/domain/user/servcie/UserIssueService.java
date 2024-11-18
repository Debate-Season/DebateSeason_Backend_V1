package com.debateseason_backend_v1.domain.user.servcie;

import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.user.dto.UserIssueDTO;
import com.debateseason_backend_v1.domain.repository.entity.User;

import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class UserIssueService {
    // User와 Issue를 조회한다, -> UserIssue에 등록을 한다.

    //
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    //
    private final UserIssueRepository userIssueRepository;
    
    // 1. userIssue에 저장하기
    public ResponseEntity<?> saveUserIssue(UserIssueDTO userIssueDTO){

        String username = userIssueDTO.getName();
        String title = userIssueDTO.getTitle();

        User user = userRepository.findByUsername(username);
        Issue issue = issueRepository.findByTitle(title);

        UserIssue userIssue = UserIssue.builder()
                .user(user)
                .issue(issue)
                .build();

        userIssueRepository.save(userIssue);

        return ResponseEntity.ok("UserIssue를 성공적을 저장했습니다.");
    }

}
