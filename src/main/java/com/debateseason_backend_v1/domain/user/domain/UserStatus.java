package com.debateseason_backend_v1.domain.user.domain;

public enum UserStatus {

	ACTIVE {
		@Override
		boolean canLogin() {
			return true;
		}

		@Override
		boolean canWithdrawalRequest() {
			return true;
		}

		@Override
		boolean canAnonymize() {
			return false;
		}
	},

	WITHDRAWAL_PENDING {
		@Override
		boolean canLogin() {
			return true;
		}

		@Override
		boolean canWithdrawalRequest() {
			return false;
		}

		@Override
		boolean canAnonymize() {
			return true;
		}
	},

	WITHDRAWAL {
		@Override
		boolean canLogin() {
			return false;
		}

		@Override
		boolean canWithdrawalRequest() {
			return false;
		}

		@Override
		boolean canAnonymize() {
			return false;
		}
	};

	abstract boolean canLogin();

	abstract boolean canWithdrawalRequest();

	abstract boolean canAnonymize();
}
