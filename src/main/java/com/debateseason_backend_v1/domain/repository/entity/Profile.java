package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.repository.entity.vo.PersonalInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "profile")
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "profile_id")
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "community_id")
	private Long communityId;

	@Column(name = "profile_image")
	private String profileImage;

	@Embedded
	PersonalInfo personalInfo;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public static Profile create(
		Long userId, Long communityId, String profileImage, PersonalInfo personalInfo
	) {

		return Profile.builder()
			.userId(userId)
			.communityId(communityId)
			.profileImage(profileImage)
			.personalInfo(personalInfo)
			.build();
	}

	public void update(
		Long communityId, String profileImage, PersonalInfo personalInfo
	) {
		
		this.profileImage = profileImage;
		this.communityId = communityId;
		this.personalInfo = personalInfo;
	}

	// public void anonymize(String anonymousNickname) {
	//
	// 	this.nickname = anonymousNickname;
	// 	this.gender = GenderType.UNDEFINED;
	// }

	public CommunityType getCommunityType() {

		return communityId != null ? CommunityType.findById(communityId) : null;
	}

}