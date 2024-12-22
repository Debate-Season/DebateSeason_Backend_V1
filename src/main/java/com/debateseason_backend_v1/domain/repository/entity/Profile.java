package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;

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
@Table(name = "profile")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "profile_id")
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "nickname")
	private String nickname;

	@Column(name = "community")
	private String community;

	@Column(name = "gender")
	private String gender;

	@Column(name = "age_range")
	private String ageRange;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder
	private Profile(Long userId, String nickname, String imageUrl,
		String community, String gender, String ageRange) {

		this.userId = userId;
		this.nickname = nickname;
		this.imageUrl = imageUrl;
		this.community = community;
		this.gender = gender;
		this.ageRange = ageRange;
	}

}
