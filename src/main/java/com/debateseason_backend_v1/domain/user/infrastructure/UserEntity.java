package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.user.domain.OAuthProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "social_type")
	private OAuthProvider OAuthProvider;

	@Column(name = "identifier")
	private String identifier;

	@Column(name = "is_deleted")
	private boolean isDeleted = false;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "update_at")
	private LocalDateTime updatedAt;

	@Builder
	private UserEntity(OAuthProvider OAuthProvider, String externalId) {

		this.OAuthProvider = OAuthProvider;
		this.identifier = externalId;
	}

	public void withdraw() {

		this.isDeleted = true;
	}

	public void restore() {

		this.isDeleted = false;
	}

	public void anonymize(String uuid) {

		this.identifier = uuid;
	}

}