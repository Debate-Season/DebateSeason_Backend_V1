package com.debateseason_backend_v1.domain.user.domain;

public interface UserStatus {
	interface Active extends UserStatus {
	}

	interface Blocked extends UserStatus {
	}

	interface Withdrawn extends UserStatus {
	}

	interface Pending extends UserStatus {
	}

	interface PendingWithdrawal extends UserStatus {
	}
}
