package com.debateseason_backend_v1.domain.user.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

class UserTest {

	@Nested
	@DisplayName("User 생성 테스트")
	class CreateTest {
		@Test
		@DisplayName("User 생성 성공")
		public void userCreationTest() {
			// given
			String providerId = "provider123";
			OAuthProvider oAuthProvider = OAuthProvider.KAKAO;

			// when
			User user = User.create(providerId, oAuthProvider);

			// then
			assertNotNull(user);
		}
	}

	@Nested
	@DisplayName("User 로그인 테스트")
	class LoginTest {
		@Test
		@DisplayName("ACTIVE 상태의 사용자 로그인 성공")
		public void loginActiveUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);

			// when & then
			// 예외가 발생하지 않아야 함
			assertDoesNotThrow(user::login);
		}

		@Test
		@DisplayName("WITHDRAWAL_PENDING 상태의 사용자 로그인 성공")
		public void loginWithdrawalPendingUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);
			user.withdraw(); // WITHDRAWAL_PENDING 상태로 변경

			// when & then
			// 예외가 발생하지 않아야 함
			assertDoesNotThrow(user::login);
		}

		@Test
		@DisplayName("WITHDRAWAL 상태의 사용자 로그인 시 예외 발생")
		public void loginWithdrawalUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);
			user.withdraw(); // WITHDRAWAL_PENDING 상태로 변경
			user.anonymize("uuid123"); // WITHDRAWAL 상태로 변경

			// when & then
			CustomException exception = assertThrows(CustomException.class, user::login);
			assertEquals(ErrorCode.NOT_LOGINABLE, exception.getCodeInterface());
		}
	}

	@Nested
	@DisplayName("User 탈퇴 요청 테스트")
	class WithdrawTest {
		@Test
		@DisplayName("ACTIVE 상태의 사용자 탈퇴 요청 성공")
		public void withdrawActiveUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);

			// when & then
			assertDoesNotThrow(user::withdraw);
		}

		@Test
		@DisplayName("WITHDRAWAL_PENDING 상태의 사용자 탈퇴 요청 시 예외 발생")
		public void withdrawWithdrawalPendingUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);
			user.withdraw(); // WITHDRAWAL_PENDING 상태로 변경

			// when & then
			CustomException exception = assertThrows(CustomException.class, user::withdraw);
			assertEquals(ErrorCode.NOT_WITHDRAWABLE, exception.getCodeInterface());
		}

		@Test
		@DisplayName("WITHDRAWAL 상태의 사용자 탈퇴 요청 시 예외 발생")
		public void withdrawWithdrawalUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);
			user.withdraw();
			user.anonymize("uuid123");

			// when & then
			CustomException exception = assertThrows(CustomException.class, user::withdraw);
			assertEquals(ErrorCode.NOT_WITHDRAWABLE, exception.getCodeInterface());
		}
	}

	@Nested
	@DisplayName("User 익명화 테스트")
	class AnonymizeTest {
		@Test
		@DisplayName("WITHDRAWAL_PENDING 상태의 사용자 익명화 성공")
		public void anonymizeWithdrawalPendingUserTest() {
			// given
			String providerId = "provider123";
			String uuid = "uuid123";
			User user = User.create(providerId, OAuthProvider.KAKAO);
			user.withdraw();

			// when & then
			assertDoesNotThrow(() -> user.anonymize(uuid));
		}

		@Test
		@DisplayName("ACTIVE 상태의 사용자 익명화 시 예외 발생")
		public void anonymizeActiveUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);

			// when & then
			CustomException exception = assertThrows(CustomException.class, () -> {
				user.anonymize("uuid123");
			});
			assertEquals(ErrorCode.NOT_ANONYMIZABLE, exception.getCodeInterface());
		}

		@Test
		@DisplayName("WITHDRAWAL 상태의 사용자 익명화 시 예외 발생")
		public void anonymizeWithdrawalUserTest() {
			// given
			User user = User.create("provider123", OAuthProvider.KAKAO);
			user.withdraw();
			user.anonymize("uuid123");

			// when & then
			CustomException exception = assertThrows(CustomException.class, () -> {
				user.anonymize("uuid456");
			});
			assertEquals(ErrorCode.NOT_ANONYMIZABLE, exception.getCodeInterface());
		}
	}
}