package com.debateseason_backend_v1.domain.user.domain;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import lombok.Builder;

public class User {

	public static final User EMPTY = new User(null, null, null, null, null);
	private static final int ANONYMIZATION_WAITING_DAYS = 5;

	private Long id;
	private String identifier;
	private SocialType socialType;
	private UserStatus status;
	private LocalDateTime updatedAt;

	@Builder
	private User(Long id, String identifier, SocialType socialType, UserStatus status, LocalDateTime updatedAt) {
		this.id = id;
		this.identifier = identifier;
		this.socialType = socialType;
		this.status = status;
		this.updatedAt = updatedAt;
	}

	public static User create(String identifier, SocialType socialType) {
		return User.builder()
			.identifier(identifier)
			.socialType(socialType)
			.status(UserStatus.ACTIVE)
			.build();
	}

	public void login() {
		if (!status.canLogin()) {
			throw new CustomException(ErrorCode.NOT_LOGINABLE);
		}

		if (status == UserStatus.WITHDRAWAL_PENDING) {
			status = UserStatus.ACTIVE;
		}
	}

	public void withdraw() {
		if (!status.canWithdrawalRequest()) {
			throw new CustomException(ErrorCode.NOT_WITHDRAWABLE);
		}

		status = UserStatus.WITHDRAWAL_PENDING;
	}

	public void anonymize(String uuid) {
		if (!status.canAnonymize()) {
			throw new CustomException(ErrorCode.NOT_ANONYMIZABLE);
		}

		identifier = uuid;
		status = UserStatus.WITHDRAWAL;
	}

	public boolean canAnonymizeBySchedule() {
		if (status != UserStatus.WITHDRAWAL_PENDING) {
			return false;
		}

		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(ANONYMIZATION_WAITING_DAYS);
		return updatedAt.isBefore(cutoffDate);
	}

	public UserMappingData getMappingData() {
		return UserMappingData.builder()
			.id(id)
			.identifier(identifier)
			.socialType(socialType)
			.status(status)
			.build();
	}

	public Long getId() {
		return this.id;
	}
}
