package com.debateseason_backend_v1.domain.userIssue;

import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;

public class UserIssueManager {

	// 1. Issue 북마크 등록하기
	public UserIssue bookMarkUserIssue(User user, Issue issue) {

		return UserIssue.builder()
			.user(user)
			.issue(issue)
			.bookmark("yes")
			.build();
	}

	// 2. bookMark 변경하기
	// Dirty-Checking을 사용하므로, JPA 의존적이다.
	public String updateBookMarkUserIssue(UserIssue userIssue){
		String bookMarkState;

		// 더티 체킹하자
		if(userIssue.getBookmark().equals("yes")){
			// no로 바꾸자
			bookMarkState="no";
			userIssue.setBookmark("no");
		}
		else{
			bookMarkState="yes";
			userIssue.setBookmark("yes");
		}

		return bookMarkState;


	}
}
