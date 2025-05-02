package com.debateseason_backend_v1.domain.user.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.enums.UserStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 도메인 객체입니다.
 * 사용자의 상태와 행동을 정의합니다.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

	private UserId id;
	private UserStatus status;
	private SocialType socialType;
	private String identifier;
	private LocalDateTime createdAt;
	private LocalDateTime updateAt;
	private LocalDateTime withdrawalRequestedAt;

	public static User create(OidcUserInfo oidcUserInfo) {

		return User.builder()
			.socialType(oidcUserInfo.socialType())
			.identifier(oidcUserInfo.identifier())
			.status(UserStatus.PENDING)
			.build();
	}

	public boolean hasProfile() {
		return this.status == UserStatus.ACTIVE;
	}

	public boolean isBlock() {
		return this.status == UserStatus.BLOCKED;
	}

	public boolean isPendingWithdrawal() {
		return this.status == UserStatus.PENDING_WITHDRAWAL;
	}

	public boolean isWithdrawn() {
		return this.status == UserStatus.WITHDRAWN;
	}

	public void restoreFromWithdrawal(LocalDateTime now) {

		if (this.status == UserStatus.PENDING_WITHDRAWAL &&
			Duration.between(this.withdrawalRequestedAt, now).toDays() < 5) {
			this.status = UserStatus.ACTIVE;
			this.withdrawalRequestedAt = null;
		}
	}

}
