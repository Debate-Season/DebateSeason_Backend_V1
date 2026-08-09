package com.debateseason_backend_v1.security.error;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * 인증 실패 로그의 <b>레벨</b>을 고정하는 테스트.
 *
 * <p>운영의 {@code scripts/error-monitor.sh} 는 {@code ' ERROR '} 가 찍힌 줄만 디스코드로 보낸다.
 * 즉 여기서 ERROR 로 올리는 순간 그 로그는 곧바로 알림이 된다. 클라이언트가 스스로 복구하는
 * 정상 흐름(만료·서명 불일치·헤더 없음)은 ERROR 가 아니어야 새벽 오탐이 사라진다.
 *
 * <p>응답 본문·상태 코드는 함께 고정한다 — 클라이언트의 401 기반 재발급이 여기에 걸려 있다.
 */
class JwtAuthenticationErrorHandlerTest {

	private static final String URI = "/prod/api/v1/users/home";

	private JwtAuthenticationErrorHandler errorHandler;
	private MockHttpServletResponse response;

	private Logger logger;
	private ListAppender<ILoggingEvent> appender;
	private Level originalLevel;

	@BeforeEach
	void setUp() {
		errorHandler = new JwtAuthenticationErrorHandler(new ObjectMapper());
		response = new MockHttpServletResponse();

		logger = (Logger)LoggerFactory.getLogger(JwtAuthenticationErrorHandler.class);
		originalLevel = logger.getLevel();
		// DEBUG 로 내린 로그도 appender 에 들어와야 "ERROR 가 아님" 을 확인할 수 있다.
		logger.setLevel(Level.TRACE);

		appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
	}

	@AfterEach
	void tearDown() {
		logger.detachAppender(appender);
		appender.stop();
		logger.setLevel(originalLevel);
	}

	@Nested
	@DisplayName("서명 불일치 - 휴면 사용자 복귀")
	class StaleSignature {

		@Test
		@DisplayName("ERROR 가 아니라 INFO 로 남긴다")
		void logsAsInfo() throws Exception {
			errorHandler.handleStaleSignatureToken(response, URI);

			ILoggingEvent event = singleEvent();
			assertThat(event.getLevel()).isEqualTo(Level.INFO);
			assertThat(event.getFormattedMessage())
				.contains("휴면 사용자 복귀")
				.contains(URI);
		}

		@Test
		@DisplayName("응답은 무효 토큰과 동일한 401 INVALID_ACCESS_TOKEN 이다 (클라이언트 재발급 트리거)")
		void respondsWithInvalidAccessToken() throws Exception {
			errorHandler.handleStaleSignatureToken(response, URI);

			assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
			assertThat(response.getContentAsString())
				.contains(ErrorCode.INVALID_ACCESS_TOKEN.name());
		}
	}

	@Nested
	@DisplayName("클라이언트가 스스로 복구하는 인증 실패")
	class ClientRecoverable {

		@Test
		@DisplayName("토큰 만료는 DEBUG - 설계된 정상 흐름이다")
		void expiredTokenIsDebug() throws Exception {
			errorHandler.handleExpiredToken(response, URI);

			assertThat(singleEvent().getLevel()).isEqualTo(Level.DEBUG);
			assertThat(response.getContentAsString())
				.contains(ErrorCode.EXPIRED_ACCESS_TOKEN.name());
		}

		@Test
		@DisplayName("토큰 없음은 DEBUG - 비로그인 클라이언트다")
		void missingTokenIsDebug() throws Exception {
			errorHandler.handleMissingToken(response, URI);

			assertThat(singleEvent().getLevel()).isEqualTo(Level.DEBUG);
		}

		@Test
		@DisplayName("형식이 깨진 토큰은 WARN - 클라이언트 결함이라 눈에는 띄어야 한다")
		void invalidTokenIsWarn() throws Exception {
			errorHandler.handleInvalidToken(response, URI);

			assertThat(singleEvent().getLevel()).isEqualTo(Level.WARN);
		}
	}

	@Test
	@DisplayName("어떤 인증 실패도 ERROR 로 남기지 않는다 - error-monitor.sh 가 ERROR 만 알림으로 보낸다")
	void neverLogsAtErrorLevel() throws Exception {
		errorHandler.handleStaleSignatureToken(response, URI);
		errorHandler.handleExpiredToken(new MockHttpServletResponse(), URI);
		errorHandler.handleInvalidToken(new MockHttpServletResponse(), URI);
		errorHandler.handleMissingToken(new MockHttpServletResponse(), URI);

		assertThat(appender.list)
			.hasSize(4)
			.noneMatch(event -> event.getLevel() == Level.ERROR);
	}

	private ILoggingEvent singleEvent() {
		List<ILoggingEvent> events = appender.list;
		assertThat(events).hasSize(1);
		return events.get(0);
	}
}
