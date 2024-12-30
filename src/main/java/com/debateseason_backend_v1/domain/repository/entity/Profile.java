package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.user.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.user.enums.GenderType;

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

	@Builder
	private Profile(Long userId, String nickname, GenderType gender, AgeRangeType ageRange
	) {

		this.userId = userId;
		this.nickname = nickname;
		this.gender = gender;
		this.ageRange = ageRange;
	}

	public void update(String nickname, GenderType gender, AgeRangeType ageRange) {
		
		if (!Objects.equals(nickname, this.nickname)) {
			this.nickname = nickname;
		}
		if (!Objects.equals(gender, this.gender)) {
			this.gender = gender;
		}
		if (!Objects.equals(ageRange, this.ageRange)) {
			this.ageRange = ageRange;
		}
	}

}