package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "refresh_token_id")
	private Long id;

	// 연관관계가 아니라 평범한 Long 이므로 @Column 이다.
	// 예전에는 @JoinColumn 이 붙어 있었는데, 기본 타입 필드에는 무시된다.
	// 그동안 컬럼명이 맞았던 건 Spring Boot 기본 네이밍 전략이 userId -> user_id 로
	// 바꿔줬기 때문이지 그 어노테이션 덕분이 아니었다. 전략이 바뀌거나 필드명을
	// 고치면 조용히 깨지는 구조라 명시한다.
	//
	// user_id 에 유니크 제약을 걸지 말 것 — 한 유저가 여러 행을 갖는 건 정상이다.
	// 로그인 1회 = 행 1개이고, 그게 기기별 세션 단위다. 아래 update() 의 회전도
	// 행 단위로 돌기 때문에, 유니크로 묶으면 기기 두 대가 서로의 토큰을 밀어내
	// 한쪽이 로그아웃된다. AuthServiceV1ReissueTest 가 이 불변식을 고정한다.
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "current_token", unique = true, nullable = false)
	private String currentToken;

	@Column(name = "previou_token", unique = true, nullable = false)
	private String previousToken;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "update_at")
	private LocalDateTime updatedAt;

	@Builder
	private RefreshToken(Long userId, String currentToken, String previousToken) {
		this.userId = userId ;
		this.currentToken = currentToken;
		this.previousToken = previousToken;
	}

	public static RefreshToken create(Long userId, String currentToken, String previousToken) {
		return RefreshToken.builder()
			.userId(userId)
			.currentToken(currentToken)
			.previousToken(previousToken)
			.build();
	}

	public void update(String newToken) {
		this.previousToken = this.currentToken;
		this.currentToken = newToken;
	}
}