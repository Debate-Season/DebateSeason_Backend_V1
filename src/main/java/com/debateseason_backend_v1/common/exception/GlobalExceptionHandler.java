package com.debateseason_backend_v1.common.exception;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.debateseason_backend_v1.common.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	// 지원하지 않는 HTTP Method 요청 시 처리
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ErrorResponse> handle(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {

		// 405 는 클라이언트가 잘못 호출한 것이다. 서버 결함이 아니므로 WARN.
		log.warn("Method not supported. method={} uri={}", request.getMethod(), request.getRequestURI());

		return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
	}

	// CustomException 발생 시 처리
	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {

		ErrorCode errorCode = (ErrorCode)e.getCodeInterface();
		HttpStatus status = errorCode.getHttpStatus();

		// 로그 레벨을 상태 코드로 가른다.
		// 4xx 는 "없는 리소스를 요청했다" 같은 클라이언트 원인이라 서버 결함이 아니다.
		// 이걸 ERROR 로 남기면 error-monitor 가 Discord 로 알림을 보내 소음이 되고,
		// 결국 진짜 장애 알림을 놓치게 된다. 5xx 만 ERROR 로 올린다.
		if (status.is5xxServerError()) {
			log.error("CustomException. code={} status={} uri={} message={}",
				errorCode.name(), status.value(), request.getRequestURI(), resolveMessage(e, errorCode), e);
		} else {
			// 4xx 는 스택트레이스도 남기지 않는다. 원인이 요청 자체라 추적 가치가 낮다.
			log.warn("CustomException. code={} status={} uri={} message={}",
				errorCode.name(), status.value(), request.getRequestURI(), resolveMessage(e, errorCode));
		}

		return createErrorResponseEntity(errorCode);
	}

	// CustomException(ErrorCode) 로 던지면 getMessage() 가 null 이라
	// 기존 로그가 "Message: null" 로 남아 무엇이 터졌는지 알 수 없었다.
	private String resolveMessage(CustomException e, ErrorCode errorCode) {
		return e.getMessage() != null ? e.getMessage() : errorCode.getMessage();
	}

	// validation 오류 발생 시 처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

		// 400 검증 실패는 항상 클라이언트 원인이다.
		String detailMessage = createDetailMessage(e.getBindingResult().getFieldErrors());

		log.warn("Validation failed. detail={}", detailMessage);

		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST, detailMessage);

		return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e){


		// 400 파라미터 형식 오류도 항상 클라이언트 원인이다.
		// 잘못된 파라미터
		Object invalidValue = e.getValue();

		// 기댓값
		Class<?> requiredType = e.getRequiredType();
		String expectedValues = "Unknown";
		if (requiredType != null && requiredType.isEnum()) {
			expectedValues = Arrays.stream(requiredType.getEnumConstants())
				.map(Object::toString)
				.collect(Collectors.joining(", "));
		}

		// 파라미터
		String parameter = e.getName();


		String detailMessage = new StringBuilder()
			.append("parameter : ")
			.append(parameter)
			.append(", expected : ")
			.append(expectedValues)
			.append(", but you typing ")
			.append(invalidValue)
			.toString()
			;

		log.warn("Parameter mismatch. detail={}", detailMessage);

		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST,detailMessage);

		return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
	}

	private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {

		return new ResponseEntity<>(
			ErrorResponse.of(errorCode),
			errorCode.getHttpStatus());
	}

	private String createDetailMessage(List<FieldError> fieldErrors) {
		return fieldErrors.stream()
			.map(error -> String.format("%s", error.getDefaultMessage()))
			.collect(Collectors.joining(", "));
	}

}