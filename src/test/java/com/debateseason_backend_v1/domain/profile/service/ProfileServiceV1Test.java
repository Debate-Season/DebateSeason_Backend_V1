package com.debateseason_backend_v1.domain.profile.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.application.service.ProfileServiceV1;
import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileRegisterServiceRequest;
import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileUpdateServiceRequest;
import com.debateseason_backend_v1.domain.profile.application.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.profile.domain.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.domain.DistrictType;
import com.debateseason_backend_v1.domain.profile.domain.GenderType;
import com.debateseason_backend_v1.domain.profile.domain.ProvinceType;
import com.debateseason_backend_v1.domain.profile.domain.Region;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileEntity;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;
import com.debateseason_backend_v1.domain.profile.validator.ProfileValidator;

@ExtendWith(MockitoExtension.class)
class ProfileServiceV1Test {

	@Mock
	private ProfileJpaRepository profileRepository;

	@Mock
	private ProfileValidator profileValidator;

	@InjectMocks
	private ProfileServiceV1 profileService;

	@Nested
	@DisplayName("프로필 등록")
	class Register {

		@Test
		@DisplayName("유효한 정보로 프로필을 등록할 수 있다")
		void registerProfileWithValidInfo() {
			// given
			ProfileRegisterServiceRequest request = createRegisterRequest();

			doNothing().when(profileValidator).validateProfileExists(anyLong());
			doNothing().when(profileValidator).validateNicknamePattern(anyString());
			doNothing().when(profileValidator).validateNicknameExists(anyString());
			doNothing().when(profileValidator).validateSupportedCommunity(anyLong());

			given(profileRepository.save(any(ProfileEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

			// when & then
			profileService.register(request);

			verify(profileValidator).validateProfileExists(request.userId());
			verify(profileValidator).validateNicknamePattern(request.nickname());
			verify(profileValidator).validateNicknameExists(request.nickname());
			verify(profileValidator).validateSupportedCommunity(request.communityId());
			verify(profileRepository, times(1)).save(any(ProfileEntity.class));
		}

		@Test
		@DisplayName("이미 존재하는 프로필이면 예외가 발생한다")
		void throwsExceptionWhenProfileAlreadyExists() {
			// given
			ProfileRegisterServiceRequest request = createRegisterRequest();

			doThrow(new CustomException(ErrorCode.ALREADY_EXIST_PROFILE))
				.when(profileValidator).validateProfileExists(anyLong());

			// when & then
			assertThatThrownBy(() -> profileService.register(request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.ALREADY_EXIST_PROFILE);

			verify(profileRepository, times(0)).save(any(ProfileEntity.class));
		}

		@Test
		@DisplayName("올바르지 않은 닉네임 패턴이면 예외가 발생한다")
		void throwsExceptionWithInvalidNicknamePattern() {
			// given
			ProfileRegisterServiceRequest request = createRegisterRequest();

			doNothing().when(profileValidator).validateProfileExists(anyLong());
			doThrow(new CustomException(ErrorCode.INVALID_NICKNAME_PATTERN))
				.when(profileValidator).validateNicknamePattern(anyString());

			// when & then
			assertThatThrownBy(() -> profileService.register(request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.INVALID_NICKNAME_PATTERN);

			verify(profileRepository, times(0)).save(any(ProfileEntity.class));
		}

		@Test
		@DisplayName("중복된 닉네임이면 예외가 발생한다")
		void throwsExceptionWithDuplicateNickname() {
			// given
			ProfileRegisterServiceRequest request = createRegisterRequest();

			doNothing().when(profileValidator).validateProfileExists(anyLong());
			doNothing().when(profileValidator).validateNicknamePattern(anyString());
			doThrow(new CustomException(ErrorCode.DUPLICATE_NICKNAME))
				.when(profileValidator).validateNicknameExists(anyString());

			// when & then
			assertThatThrownBy(() -> profileService.register(request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.DUPLICATE_NICKNAME);

			verify(profileRepository, times(0)).save(any(ProfileEntity.class));
		}

		@Test
		@DisplayName("지원하지 않는 커뮤니티면 예외가 발생한다")
		void throwsExceptionWithUnsupportedCommunity() {
			// given
			ProfileRegisterServiceRequest request = createRegisterRequest();

			doNothing().when(profileValidator).validateProfileExists(anyLong());
			doNothing().when(profileValidator).validateNicknamePattern(anyString());
			doNothing().when(profileValidator).validateNicknameExists(anyString());
			doThrow(new CustomException(ErrorCode.NOT_SUPPORTED_COMMUNITY))
				.when(profileValidator).validateSupportedCommunity(anyLong());

			// when & then
			assertThatThrownBy(() -> profileService.register(request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_SUPPORTED_COMMUNITY);

			verify(profileRepository, times(0)).save(any(ProfileEntity.class));
		}
	}

	@Nested
	@DisplayName("프로필 조회")
	class GetProfile {

		@Test
		@DisplayName("사용자 ID로 프로필을 조회할 수 있다")
		void getProfileByUserId() {
			// given
			Long userId = 1L;
			ProfileEntity profile = createProfile();

			given(profileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

			// when
			ProfileResponse response = profileService.getProfileByUserId(userId);

			// then
			assertThat(response).isNotNull();
			assertThat(response.nickname()).isEqualTo(profile.getNickname());
			assertThat(response.gender()).isEqualTo(profile.getGender());
			assertThat(response.ageRange()).isEqualTo(profile.getAgeRange());
			assertThat(response.community().id()).isEqualTo(profile.getCommunityId());
		}

		@Test
		@DisplayName("존재하지 않는 사용자면 예외가 발생한다")
		void throwsExceptionWhenUserNotExists() {
			// given
			Long userId = 999L;

			given(profileRepository.findByUserId(userId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> profileService.getProfileByUserId(userId))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_FOUND_PROFILE);
		}
	}

	@Nested
	@DisplayName("프로필 업데이트")
	class Update {

		@Test
		@DisplayName("유효한 정보로 프로필을 업데이트할 수 있다")
		void updateProfileWithValidInfo() {
			// given
			ProfileUpdateServiceRequest request = createUpdateRequest();
			ProfileEntity profile = createProfile();

			given(profileRepository.findByUserId(request.userId())).willReturn(Optional.of(profile));
			doNothing().when(profileValidator).validateSupportedCommunity(anyLong());

			// when
			profileService.update(request);

			// then
			verify(profileRepository, times(1)).findByUserId(request.userId());
			verify(profileValidator, times(1)).validateSupportedCommunity(request.communityId());
		}

		@Test
		@DisplayName("프로필이 존재하지 않으면 예외가 발생한다")
		void throwsExceptionWhenProfileNotFound() {
			// given
			ProfileUpdateServiceRequest request = createUpdateRequest();

			given(profileRepository.findByUserId(request.userId())).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> profileService.update(request))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_FOUND_PROFILE);
		}

		@Test
		@DisplayName("닉네임을 변경할 때 중복 검사를 수행한다")
		void validateNicknameWhenChanged() {
			// given
			ProfileUpdateServiceRequest request = createUpdateRequest();
			ProfileEntity profile = ProfileEntity.builder()
				.userId(1L)
				.profileImage("RED")
				.nickname("기존닉네임")
				.communityId(1L)
				.gender(GenderType.MALE)
				.ageRange(AgeRangeType.TWENTIES)
				.build();

			given(profileRepository.findByUserId(request.userId())).willReturn(Optional.of(profile));
			doNothing().when(profileValidator).validateSupportedCommunity(anyLong());
			doNothing().when(profileValidator).validateNicknamePattern(anyString());
			doNothing().when(profileValidator).validateNicknameExists(anyString());

			// when
			profileService.update(request);

			// then
			verify(profileValidator, times(1)).validateNicknamePattern(request.nickname());
			verify(profileValidator, times(1)).validateNicknameExists(request.nickname());
		}

		@Test
		@DisplayName("닉네임이 변경되지 않으면 중복 검사를 수행하지 않는다")
		void skipNicknameValidationWhenNotChanged() {
			// given
			String sameNickname = "토론왕";
			ProfileUpdateServiceRequest request = ProfileUpdateServiceRequest.builder()
				.userId(1L)
				.profileImage("RED")
				.nickname(sameNickname)
				.communityId(1L)
				.gender(GenderType.MALE)
				.ageRange(AgeRangeType.TWENTIES)
				.hometownDistrict(DistrictType.YONGSAN)
				.hometownProvince(ProvinceType.SEOUL)
				.residenceDistrict(DistrictType.YONGSAN)
				.residenceProvince(ProvinceType.SEOUL)
				.build();

			ProfileEntity profile = ProfileEntity.builder()
				.userId(1L)
				.profileImage("RED")
				.nickname(sameNickname)
				.communityId(1L)
				.gender(GenderType.MALE)
				.ageRange(AgeRangeType.TWENTIES)
				.build();

			given(profileRepository.findByUserId(request.userId())).willReturn(Optional.of(profile));
			doNothing().when(profileValidator).validateSupportedCommunity(anyLong());

			// when
			profileService.update(request);

			// then
			verify(profileValidator, times(0)).validateNicknamePattern(anyString());
			verify(profileValidator, times(0)).validateNicknameExists(anyString());
		}
	}

	@Nested
	@DisplayName("닉네임 중복 확인")
	class CheckNicknameAvailability {

		@Test
		@DisplayName("유효한 닉네임은 중복 확인을 통과한다")
		void validNicknamePassesCheck() {
			// given
			String nickname = "토론왕";

			doNothing().when(profileValidator).validateNicknamePattern(nickname);
			doNothing().when(profileValidator).validateNicknameExists(nickname);

			// when & then
			profileService.checkNicknameAvailability(nickname);

			verify(profileValidator, times(1)).validateNicknamePattern(nickname);
			verify(profileValidator, times(1)).validateNicknameExists(nickname);
		}

		@Test
		@DisplayName("올바르지 않은 닉네임 패턴이면 예외가 발생한다")
		void throwsExceptionWithInvalidPattern() {
			// given
			String invalidNickname = "Invalid!@#";

			doThrow(new CustomException(ErrorCode.INVALID_NICKNAME_PATTERN))
				.when(profileValidator).validateNicknamePattern(invalidNickname);

			// when & then
			assertThatThrownBy(() -> profileService.checkNicknameAvailability(invalidNickname))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.INVALID_NICKNAME_PATTERN);
		}

		@Test
		@DisplayName("중복된 닉네임이면 예외가 발생한다")
		void throwsExceptionWithDuplicateNickname() {
			// given
			String duplicateNickname = "이미사용중";

			doNothing().when(profileValidator).validateNicknamePattern(duplicateNickname);
			doThrow(new CustomException(ErrorCode.DUPLICATE_NICKNAME))
				.when(profileValidator).validateNicknameExists(duplicateNickname);

			// when & then
			assertThatThrownBy(() -> profileService.checkNicknameAvailability(duplicateNickname))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.DUPLICATE_NICKNAME);
		}
	}

	// 테스트 데이터 생성 헬퍼 메서드
	private ProfileRegisterServiceRequest createRegisterRequest() {
		return ProfileRegisterServiceRequest.builder()
			.userId(1L)
			.profileImage("RED")
			.nickname("토론왕")
			.communityId(1L)
			.gender(GenderType.MALE)
			.ageRange(AgeRangeType.TWENTIES)
			.hometownDistrict(DistrictType.YONGSAN)
			.hometownProvince(ProvinceType.SEOUL)
			.residenceDistrict(DistrictType.YONGSAN)
			.residenceProvince(ProvinceType.SEOUL)
			.build();
	}

	private ProfileUpdateServiceRequest createUpdateRequest() {
		return ProfileUpdateServiceRequest.builder()
			.userId(1L)
			.profileImage("BLUE")
			.nickname("토론왕2")
			.communityId(2L)
			.gender(GenderType.FEMALE)
			.ageRange(AgeRangeType.THIRTIES)
			.hometownDistrict(DistrictType.YONGSAN)
			.hometownProvince(ProvinceType.SEOUL)
			.residenceDistrict(DistrictType.YONGSAN)
			.residenceProvince(ProvinceType.SEOUL)
			.build();
	}

	private ProfileEntity createProfile() {
		return ProfileEntity.builder()
			.userId(1L)
			.profileImage("RED")
			.nickname("토론왕")
			.communityId(1L)
			.gender(GenderType.MALE)
			.ageRange(AgeRangeType.TWENTIES)
			.hometown(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.residence(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.build();
	}
}