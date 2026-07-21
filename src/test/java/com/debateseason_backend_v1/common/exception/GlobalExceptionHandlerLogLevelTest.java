package com.debateseason_backend_v1.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * 로그 레벨이 곧 알림 정책이다.
 * error-monitor.sh 가 ERROR 만 Discord 로 보내므로, 클라이언트 원인인 4xx 가
 * ERROR 로 남으면 알림 소음이 되고 진짜 장애를 놓치게 된다.
 */
@DisplayName("예외 로그 레벨")
class GlobalExceptionHandlerLogLevelTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	private Logger logger;
	private ListAppender<ILoggingEvent> appender;

	@BeforeEach
	void attachAppender() {
		logger = (Logger)LoggerFactory.getLogger(GlobalExceptionHandler.class);
		appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
	}

	@AfterEach
	void detachAppender() {
		logger.detachAppender(appender);
	}

	@Test
	@DisplayName("4xx 는 WARN 으로 남아 알림 대상이 되지 않는다")
	void clientErrorIsLoggedAsWarn() {
		// 없는 채팅방 조회 — 이번 오탐 알림을 만든 바로 그 케이스
		handler.handleCustomException(
			new CustomException(ErrorCode.NOT_FOUND_CHATROOM),
			request("/prod/api/v1/room")
		);

		assertThat(levels()).containsExactly(Level.WARN);
	}

	@Test
	@DisplayName("5xx 는 ERROR 로 남아 알림 대상이 된다")
	void serverErrorIsLoggedAsError() {
		handler.handleCustomException(
			new CustomException(ErrorCode.INTERNAL_SERVER_ERROR),
			request("/prod/api/v1/anything")
		);

		assertThat(levels()).containsExactly(Level.ERROR);
	}

	@Test
	@DisplayName("메시지 없이 던져도 ErrorCode 의 기본 메시지가 남는다")
	void messageFallsBackToErrorCodeMessage() {
		handler.handleCustomException(
			new CustomException(ErrorCode.NOT_FOUND_CHATROOM),
			request("/prod/api/v1/room")
		);

		// 기존에는 "Message: null" 로 남아 무엇이 터졌는지 알 수 없었다.
		String message = appender.list.get(0).getFormattedMessage();
		assertThat(message)
			.contains("NOT_FOUND_CHATROOM")
			.contains("/prod/api/v1/room")
			.doesNotContain("null");
	}

	@Test
	@DisplayName("지원하지 않는 HTTP 메서드는 WARN 이다")
	void methodNotSupportedIsWarn() {
		handler.handle(
			new org.springframework.web.HttpRequestMethodNotSupportedException("DELETE"),
			request("/prod/api/v1/room")
		);

		assertThat(levels()).containsExactly(Level.WARN);
	}

	private MockHttpServletRequest request(String uri) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(uri);
		request.setMethod("GET");
		return request;
	}

	private List<Level> levels() {
		return appender.list.stream().map(ILoggingEvent::getLevel).toList();
	}
}
