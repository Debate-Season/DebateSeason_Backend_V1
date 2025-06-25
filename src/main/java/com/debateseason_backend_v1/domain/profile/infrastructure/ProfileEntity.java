package com.debateseason_backend_v1.domain.profile.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.profile.domain.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.domain.CommunityType;
import com.debateseason_backend_v1.domain.profile.domain.GenderType;
import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.profile.domain.Region;

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
public class ProfileEntity {

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
	private ProfileEntity(
		Long id, Long userId, String profileImage, String nickname, Long communityId, GenderType gender,
		AgeRangeType ageRange,
		Region residence, Region hometown
	) {
		this.id = id;
		this.userId = userId;
		this.profileImage = profileImage;
		this.nickname = nickname;
		this.communityId = communityId;
		this.gender = gender;
		this.ageRange = ageRange;
		this.residence = residence;
		this.hometown = hometown;
	}

	public CommunityType getCommunityType() {
		return communityId != null ? CommunityType.findById(communityId) : null;
	}

	public static ProfileEntity from(Profile profile) {
		return ProfileEntity.builder()
			.id(profile.getId())
			.userId(profile.getUserId())
			.communityId(profile.getCommunityId())
			.profileImage(profile.getProfileImage())
			.nickname(profile.getNickname().value())
			.gender(profile.getGender())
			.ageRange(profile.getAgeRange())
			.residence(
				Region.of(profile.getResidence().getProvinceType(), profile.getResidence().getDistrictType())
			)
			.hometown(
				Region.of(profile.getHometown().getProvinceType(), profile.getHometown().getDistrictType())
			)
			.build();
	}

	public Profile toModel() {
		return Profile.builder()
			.id(id)
			.userId(userId)
			.communityId(communityId)
			.profileImage(profileImage)
			.nickname(Nickname.of(nickname))
			.gender(gender)
			.ageRange(ageRange)
			.residence(residence)
			.hometown(hometown)
			.build();
	}
}