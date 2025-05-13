package com.debateseason_backend_v1.domain.user.domain;

public enum UserStatus {
	PENDING {
		@Override
		public boolean isAccessible() {
			return true;
		}
	},
	ACTIVE {
		@Override
		public boolean isAccessible() {
			return true;
		}
	},
	BLOCKED {
		@Override
		public boolean isAccessible() {
			return false;
		}
	},
	WITHDRAWN_PENDING {
		@Override
		public boolean isAccessible() {
			return true;
		}
	},
	WITHDRAWN {
		@Override
		public boolean isAccessible() {
			return false;
		}
	};

	abstract boolean isAccessible();
}
