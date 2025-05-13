package com.debateseason_backend_v1.domain.user.domain;

public enum UserStatus {
	PENDING {
		@Override
		public boolean isNotRegistrable() {
			return true;
		}

		@Override
		public boolean isNotLoginable() {
			return false;
		}

		@Override
		public boolean isNotWithdrawable() {
			return false;
		}

		@Override
		public boolean isNotAnonymizable() {
			return true;
		}
	},
	ACTIVE {
		@Override
		public boolean isNotRegistrable() {
			return true;
		}

		@Override
		public boolean isNotLoginable() {
			return false;
		}

		@Override
		public boolean isNotWithdrawable() {
			return false;
		}

		@Override
		public boolean isNotAnonymizable() {
			return true;
		}
	},
	BLOCKED {
		@Override
		public boolean isNotRegistrable() {
			return true;
		}

		@Override
		public boolean isNotLoginable() {
			return true;
		}

		@Override
		public boolean isNotWithdrawable() {
			return true;
		}

		@Override
		public boolean isNotAnonymizable() {
			return true;
		}
	},
	WITHDRAW_PENDING {
		@Override
		public boolean isNotRegistrable() {
			return true;
		}

		@Override
		public boolean isNotLoginable() {
			return false;
		}

		@Override
		public boolean isNotWithdrawable() {
			return true;
		}

		@Override
		public boolean isNotAnonymizable() {
			return false;
		}
	},
	WITHDRAW {
		@Override
		public boolean isNotRegistrable() {
			return true;
		}

		@Override
		public boolean isNotLoginable() {
			return true;
		}

		@Override
		public boolean isNotWithdrawable() {
			return true;
		}

		@Override
		public boolean isNotAnonymizable() {
			return true;
		}
	};

	abstract boolean isNotRegistrable();

	abstract boolean isNotLoginable();

	abstract boolean isNotWithdrawable();

	abstract boolean isNotAnonymizable();

}
