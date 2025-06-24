package com.debateseason_backend_v1.domain.profile.validator;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;

@ExtendWith(MockitoExtension.class)
class ProfileValidatorTest {

	@Mock
	private ProfileJpaRepository profileRepository;

	@InjectMocks
	private ProfileValidator profileValidator;

	@Nested
	@DisplayName("프로필 존재 여부 검증")
	class ValidateProfileExists {

		@Test
		@DisplayName("프로필이 존재하지 않으면 검증을 통과한다")
		void passesWhenProfileDoesNotExist() {
			// given
			Long userId = 1L;
			given(profileRepository.existsByUserId(userId)).willReturn(false);

			// when & then
			assertThatCode(() -> profileValidator.validateProfileExists(userId))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("프로필이 이미 존재하면 예외가 발생한다")
		void throwsExceptionWhenProfileExists() {
			// given
			Long userId = 1L;
			given(profileRepository.existsByUserId(userId)).willReturn(true);

			// when & then
			assertThatThrownBy(() -> profileValidator.validateProfileExists(userId))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.ALREADY_EXIST_PROFILE);
		}
	}

	@Nested
	@DisplayName("지원되는 커뮤니티 검증")
	class ValidateSupportedCommunity {

		@Test
		@DisplayName("유효한 커뮤니티 ID는 검증을 통과한다")
		void passesWithValidCommunityId() {
			// given
			Long validCommunityId = 1L; // DC_INSIDE

			// when & then
			assertThatCode(() -> profileValidator.validateSupportedCommunity(validCommunityId))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("유효하지 않은 커뮤니티 ID는 예외가 발생한다")
		void throwsExceptionWithInvalidCommunityId() {
			// given
			Long invalidCommunityId = 9999L;

			// when & then
			assertThatThrownBy(() -> profileValidator.validateSupportedCommunity(invalidCommunityId))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_SUPPORTED_COMMUNITY);
		}
	}

	@Nested
	@DisplayName("닉네임 패턴 검증")
	class ValidateNicknamePattern {

		@ParameterizedTest
		@ValueSource(strings = {"토론왕", "가나다라", "abcdef", "Debate", "한글영문"})
		@DisplayName("유효한 닉네임 패턴은 검증을 통과한다")
		void passesWithValidNicknamePattern(String validNickname) {
			// when & then
			assertThatCode(() -> profileValidator.validateNicknamePattern(validNickname))
				.doesNotThrowAnyException();
		}

		@ParameterizedTest
		@ValueSource(strings = {"토론왕!!", "가나다라123", "abc def", "Debate?", "한글_영문", "닉네임이너무길어요아홉글자"})
		@DisplayName("유효하지 않은 닉네임 패턴은 예외가 발생한다")
		void throwsExceptionWithInvalidNicknamePattern(String invalidNickname) {
			// when & then
			assertThatThrownBy(() -> profileValidator.validateNicknamePattern(invalidNickname))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.INVALID_NICKNAME_PATTERN);
		}
	}

	@Nested
	@DisplayName("닉네임 중복 검증")
	class ValidateNicknameExists {

		@Test
		@DisplayName("중복되지 않은 닉네임은 검증을 통과한다")
		void passesWithNonDuplicateNickname() {
			// given
			String nickname = "토론왕";
			given(profileRepository.existsByNickname(nickname)).willReturn(false);

			// when & then
			assertThatCode(() -> profileValidator.validateNicknameExists(nickname))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("중복된 닉네임은 예외가 발생한다")
		void throwsExceptionWithDuplicateNickname() {
			// given
			String nickname = "토론왕";
			given(profileRepository.existsByNickname(nickname)).willReturn(true);

			// when & then
			assertThatThrownBy(() -> profileValidator.validateNicknameExists(nickname))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.DUPLICATE_NICKNAME);
		}
	}
}