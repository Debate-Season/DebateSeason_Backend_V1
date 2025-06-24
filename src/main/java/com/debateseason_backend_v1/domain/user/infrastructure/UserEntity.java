package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.user.domain.SocialType;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserMappingData;
import com.debateseason_backend_v1.domain.user.domain.UserStatus;

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

	@Column(name = "identifier", unique = true, nullable = false)
	private String identifier;

	@Enumerated(EnumType.STRING)
	@Column(name = "social_type", nullable = false)
	private SocialType socialType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private UserStatus status;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "update_at")
	private LocalDateTime updatedAt;

	@Builder
	private UserEntity(Long id, String identifier, SocialType socialType, UserStatus status) {
		this.id = id;
		this.identifier = identifier;
		this.socialType = socialType;
		this.status = status;
	}

	public static UserEntity from(User user) {
		UserMappingData data = user.getMappingData();

		return UserEntity.builder()
			.id(data.id())
			.identifier(data.identifier())
			.socialType(data.socialType())
			.status(data.status())
			.build();
	}

	public User toModel() {
		return User.builder()
			.id(id)
			.identifier(identifier)
			.socialType(socialType)
			.status(status)
			.updatedAt(updatedAt)
			.build();
	}
}