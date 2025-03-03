package com.debateseason_backend_v1.domain.issue.docs;

import org.springframework.http.HttpStatus;

import com.debateseason_backend_v1.common.exception.CodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum CustomErrorCode implements CodeInterface {

	NOT_FOUND_ISSUE(400, HttpStatus.NOT_FOUND, "해당 이슈방을 찾을 수 없습니다."),
	NOT_FOUND_ISSUE_WITH_CATEGORY(400, HttpStatus.NOT_FOUND, "해당 category의 이슈방을 찾을 수 없습니다.");

	private Integer code;
	private HttpStatus httpStatus;
	private String message;
}
