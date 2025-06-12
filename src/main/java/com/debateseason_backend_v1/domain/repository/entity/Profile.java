package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.profile.domain.Region;
import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

	@Column(name = "profile_image")
	private String profileImage;

	@Column(name = "nickname", unique = true)
	private String nickname;

	@Column(name = "community_id")
	private Long communityId;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender")
	private GenderType gender;

	@Enumerated(EnumType.STRING)
	@Column(name = "age_range")
	private AgeRangeType ageRange;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "provinceType", column = @Column(name = "residence_province")),
		@AttributeOverride(name = "districtType", column = @Column(name = "residence_district"))
	})
	private Region residence;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "provinceType", column = @Column(name = "hometown_province")),
		@AttributeOverride(name = "districtType", column = @Column(name = "hometown_district"))
	})
	private Region hometown;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Builder
	private Profile(
		Long userId, String profileImage, String nickname, Long communityId, GenderType gender, AgeRangeType ageRange,
		Region residence, Region hometown
	) {

		this.userId = userId;
		this.profileImage = profileImage;
		this.nickname = nickname;
		this.communityId = communityId;
		this.gender = gender;
		this.ageRange = ageRange;
		this.residence = residence;
		this.hometown = hometown;
	}

	public void update(
		String profileImage, String nickname, Long communityId, GenderType gender, AgeRangeType ageRange,
		Region residence, Region hometown
	) {

		this.profileImage = profileImage;
		this.nickname = nickname;
		this.communityId = communityId;
		this.gender = gender;
		this.ageRange = ageRange;
		this.residence = residence;
		this.hometown = hometown;
	}

	public void anonymize(String anonymousNickname) {

		this.nickname = anonymousNickname;
		this.gender = GenderType.UNDEFINED;
	}

	public CommunityType getCommunityType() {

		return communityId != null ? CommunityType.findById(communityId) : null;
	}

}