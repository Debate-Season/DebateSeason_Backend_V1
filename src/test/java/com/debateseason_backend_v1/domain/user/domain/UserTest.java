package com.debateseason_backend_v1.domain.user.domain;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.user.enums.SocialType;

class UserTest {

	private TokenIssuer tokenIssuer;

	@BeforeEach
	void setUp() {
		tokenIssuer = mock(TokenIssuer.class);
	}

	@Nested
	@DisplayName("register() 메서드 테스트")
	class RegisterTest {

		@Test
		@DisplayName("상태가 EMPTY 일 때 등록 가능하다")
		void registerWithNullStatus() {
			// given
			User user = User.EMPTY;

			// when
			User registeredUser = user.register("socialId123", SocialType.KAKAO);

			// then
			assertThat(registeredUser.getSocialAuthInfo().socialId()).isEqualTo("socialId123");
			assertThat(registeredUser.getSocialAuthInfo().socialType()).isEqualTo(SocialType.KAKAO);
			assertThat(registeredUser.getStatus()).isEqualTo(UserStatus.PENDING);
		}
	}

	@Nested
	@DisplayName("login() 메서드 테스트")
	class LoginTest {

		@ParameterizedTest
		@EnumSource(value = UserStatus.class, names = {"PENDING", "ACTIVE", "WITHDRAW_PENDING"})
		@DisplayName("로그인 가능한 상태에서 로그인할 수 있다")
		void loginWithLoginableStatus(UserStatus status) {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, status);

			// when & then
			user.login(); // 예외가 발생하지 않으면 성공
		}

		@Test
		@DisplayName("WITHDRAW_PENDING 상태에서 로그인하면 ACTIVE 상태로 변경된다")
		void loginWithWithdrawPendingStatus() {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, UserStatus.WITHDRAW_PENDING);

			// when
			user.login();

			// then
			assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		}

		@ParameterizedTest
		@EnumSource(value = UserStatus.class, names = {"BLOCKED", "WITHDRAW"})
		@DisplayName("로그인 불가능한 상태에서는 로그인할 수 없다")
		void loginWithNotLoginableStatus(UserStatus status) {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, status);

			// when & then
			assertThatThrownBy(user::login)
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_LOGINABLE);
		}
	}

	@Nested
	@DisplayName("withdraw() 메서드 테스트")
	class WithdrawTest {

		@ParameterizedTest
		@EnumSource(value = UserStatus.class, names = {"PENDING", "ACTIVE"})
		@DisplayName("탈퇴 가능한 상태에서 탈퇴할 수 있다")
		void withdrawWithWithdrawableStatus(UserStatus status) {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, status);

			// when
			user.withdraw();

			// then
			assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAW_PENDING);
		}

		@ParameterizedTest
		@EnumSource(value = UserStatus.class, names = {"BLOCKED", "WITHDRAW_PENDING", "WITHDRAW"})
		@DisplayName("탈퇴 불가능한 상태에서는 탈퇴할 수 없다")
		void withdrawWithNotWithdrawableStatus(UserStatus status) {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, status);

			// when & then
			assertThatThrownBy(user::withdraw)
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_WITHDRAWABLE);
		}
	}

	@Nested
	@DisplayName("anonymize() 메서드 테스트")
	class AnonymizeTest {

		@Test
		@DisplayName("익명화 가능한 상태에서 익명화할 수 있다")
		void anonymizeWithAnonymizableStatus() {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, UserStatus.WITHDRAW_PENDING);
			String uuid = "test-uuid-12345";

			// when
			User anonymizedUser = user.anonymize(uuid);

			// then
			assertThat(anonymizedUser).isNotNull();
		}

		@ParameterizedTest
		@EnumSource(value = UserStatus.class, names = {"PENDING", "ACTIVE", "BLOCKED", "WITHDRAW"})
		@DisplayName("익명화 불가능한 상태에서는 익명화할 수 없다")
		void anonymizeWithNotAnonymizableStatus(UserStatus status) {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, status);
			String uuid = "test-uuid-12345";

			// when & then
			assertThatThrownBy(() -> user.anonymize(uuid))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_ANONYMIZABLE);
		}
	}

	@Nested
	@DisplayName("hasProfile() 메서드 테스트")
	class HasProfileTest {

		@Test
		@DisplayName("PENDING 상태에서는 프로필이 없다")
		void hasProfileWithPendingStatus() {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, UserStatus.PENDING);

			// when
			boolean hasProfile = user.hasProfile();

			// then
			assertThat(hasProfile).isFalse();
		}

		@ParameterizedTest
		@EnumSource(value = UserStatus.class, names = {"ACTIVE", "BLOCKED", "WITHDRAW_PENDING", "WITHDRAW"})
		@DisplayName("PENDING이 아닌 상태에서는 프로필이 있다")
		void hasProfileWithNonPendingStatus(UserStatus status) {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, status);

			// when
			boolean hasProfile = user.hasProfile();

			// then
			assertThat(hasProfile).isTrue();
		}
	}

	@Nested
	@DisplayName("issueTokens() 메서드 테스트")
	class IssueTokensTest {

		@Test
		@DisplayName("토큰을 발급할 수 있다")
		void issueTokens() {
			// given
			User user = new User(1L, "socialId", SocialType.KAKAO, UserStatus.ACTIVE);
			TokenPair expectedTokenPair = new TokenPair("accessToken", "refreshToken");
			when(tokenIssuer.issueTokenPair(user.getId())).thenReturn(expectedTokenPair);

			// when
			TokenPair tokenPair = user.issueTokens(tokenIssuer);

			// then
			assertThat(tokenPair).isEqualTo(expectedTokenPair);
			assertThat(tokenPair.accessToken()).isEqualTo("accessToken");
			assertThat(tokenPair.refreshToken()).isEqualTo("refreshToken");
		}
	}

	@Nested
	@DisplayName("User 생성 테스트")
	class UserCreationTest {

		@Test
		@DisplayName("정상적으로 User를 생성할 수 있다")
		void createUser() {
			// when
			User user = new User(1L, "socialId", SocialType.KAKAO, UserStatus.ACTIVE);

			// then
			assertThat(user.getId().value()).isEqualTo(1L);
			assertThat(user.getSocialAuthInfo().socialId()).isEqualTo("socialId");
			assertThat(user.getSocialAuthInfo().socialType()).isEqualTo(SocialType.KAKAO);
			assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		}

		@Test
		@DisplayName("EMPTY 상수로 빈 User를 만들 수 있다")
		void createEmptyUser() {
			// when
			User emptyUser = User.EMPTY;

			// then
			assertThat(emptyUser).isNotNull();
		}
	}
}