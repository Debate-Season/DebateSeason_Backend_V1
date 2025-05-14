package com.debateseason_backend_v1.domain.profile.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.profile.domain.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.domain.CommunityId;
import com.debateseason_backend_v1.domain.profile.domain.CommunityType;
import com.debateseason_backend_v1.domain.profile.domain.GenderType;
import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.PersonalInfo;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.profile.domain.ProfileId;
import com.debateseason_backend_v1.domain.user.domain.UserId;

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
public class ProfileEntity {

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

	@Column(name = "nickname", unique = true)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender")
	private GenderType gender;

	@Enumerated(EnumType.STRING)
	@Column(name = "age_range")
	private AgeRangeType ageRange;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public CommunityType getCommunityType() {

		return communityId != null ? CommunityType.findById(communityId) : null;
	}

	public static ProfileEntity from(Profile profile) {
		return ProfileEntity.builder()
			.id(profile.getId().value())
			.userId(profile.getUserId().value())
			.communityId(profile.getCommunityId().value())
			.profileImage(profile.getPersonalInfo().profileImage())
			.nickname(profile.getPersonalInfo().nickname().value())
			.gender(profile.getPersonalInfo().gender())
			.ageRange(profile.getPersonalInfo().ageRange())
			.build();
	}

	public Profile toModel() {
		return new Profile(
			new ProfileId(id),
			new UserId(userId),
			new CommunityId(communityId),
			new PersonalInfo(profileImage, new Nickname(nickname), gender, ageRange)
		);
	}
}