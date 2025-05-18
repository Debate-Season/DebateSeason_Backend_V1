package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.user.domain.RefreshToken;

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
public class RefreshTokenEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "refresh_token_id")
	private Long id;

	private Long userId;

	@Column(name = "token")
	private String token;

	@Column(name = "expiration_at")
	private LocalDateTime expirationAt;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Builder
	protected RefreshTokenEntity(Long userId, String token, LocalDateTime expirationAt) {

		this.userId = userId;
		this.token = token;
		this.expirationAt = expirationAt;
	}

	public static RefreshTokenEntity from(RefreshToken refreshToken) {
		return RefreshTokenEntity.builder()
			//.userId(refreshToken.getUserId())
			.token(refreshToken.getToken())
			.expirationAt(refreshToken.getExpirationAt())
			.build();
	}

	public RefreshToken toModel() {
		return RefreshToken.builder()
			.id(this.id)
			//.userId(this.userId)
			.token(this.token)
			.expirationAt(this.expirationAt)
			.build();
	}
}