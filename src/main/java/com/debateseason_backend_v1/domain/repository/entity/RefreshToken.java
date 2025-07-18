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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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

	@JoinColumn(name = "user_id", nullable = false)
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