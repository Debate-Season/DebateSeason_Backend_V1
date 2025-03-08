package com.debateseason_backend_v1.domain.repository.entity.vo;

import org.springframework.util.Assert;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalInfo {

	@Column(name = "nickname", unique = true)
	private String nickname;

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

	public static PersonalInfo of(
		String nickname, GenderType gender, AgeRangeType ageRange, Region residence, Region hometown
	) {

		Assert.hasText(nickname, "닉네임은 필수이며 공백일 수 없습니다.");
		Assert.notNull(gender, "성별은 필수입니다.");
		Assert.notNull(ageRange, "연령대는 필수입니다.");
		Assert.notNull(residence, "거주지 정보는 필수입니다.");
		Assert.notNull(hometown, "출신지 정보는 필수입니다.");

		return PersonalInfo.builder()
			.nickname(nickname)
			.gender(gender)
			.ageRange(ageRange)
			.residence(residence)
			.hometown(hometown)
			.build();
	}

}
