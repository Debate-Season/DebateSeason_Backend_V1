package com.debateseason_backend_v1.domain.user.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.debateseason_backend_v1.common.component.UuidShortener;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.user.enums.SocialType;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserSchedulerTest {

	@Mock
	private UuidShortener uuidShortener;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProfileRepository profileRepository;

	@InjectMocks
	private UserScheduler userScheduler;

	@Test
	@DisplayName("탈퇴 후 5일이 지난 회원들이 정상적으로 익명화된다")
	void anonymizeExpiredUsersSuccessfully() {
		// given
		LocalDateTime now = LocalDateTime.of(2024, 1, 21, 0, 0);
		LocalDateTime cutoffDate = now.minusDays(5);

		User user1 = createUser(1L, "old-id-1", true, now.minusDays(6));
		User user2 = createUser(2L, "old-id-2", true, now.minusDays(7));
		List<User> expiredUsers = Arrays.asList(user1, user2);

		Profile profile1 = createProfile(1L, 1L, "old-nickname-1");
		Profile profile2 = createProfile(2L, 2L, "old-nickname-2");

		when(userRepository.findByIsDeletedTrueAndUpdatedAtBefore(any(LocalDateTime.class)))
			.thenReturn(expiredUsers);
		when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile1));
		when(profileRepository.findByUserId(2L)).thenReturn(Optional.of(profile2));
		when(uuidShortener.shortenUuid())
			.thenReturn("short-1")
			.thenReturn("short-2");

		// when
		try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
			mockedStatic.when(LocalDateTime::now).thenReturn(now);
			userScheduler.anonymizeExpiredUsers();
		}

		// then
		verify(userRepository).findByIsDeletedTrueAndUpdatedAtBefore(cutoffDate);
		verify(profileRepository, times(2)).findByUserId(any(Long.class));

		assertAll(
			() -> assertEquals("short-1", user1.getIdentifier()),
			() -> assertEquals("short-2", user2.getIdentifier()),
			() -> assertEquals("탈퇴회원#short-1", profile1.getNickname()),
			() -> assertEquals("탈퇴회원#short-2", profile2.getNickname()),
			() -> assertEquals(GenderType.UNDEFINED, profile1.getGender()),
			() -> assertEquals(GenderType.UNDEFINED, profile2.getGender())
		);
	}

	@Test
	@DisplayName("프로필이 없는 탈퇴 회원도 정상적으로 익명화된다")
	void anonymizeExpiredUsersWithoutProfile() {
		// given
		LocalDateTime now = LocalDateTime.of(2024, 1, 21, 0, 0);
		LocalDateTime cutoffDate = now.minusDays(5);

		User user = createUser(1L, "old-id", true, now.minusDays(6));
		List<User> expiredUsers = Collections.singletonList(user);

		when(userRepository.findByIsDeletedTrueAndUpdatedAtBefore(any(LocalDateTime.class)))
			.thenReturn(expiredUsers);
		when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
		when(uuidShortener.shortenUuid()).thenReturn("short-uuid");

		// when
		try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
			mockedStatic.when(LocalDateTime::now).thenReturn(now);
			userScheduler.anonymizeExpiredUsers();
		}

		// then
		verify(userRepository).findByIsDeletedTrueAndUpdatedAtBefore(cutoffDate);
		verify(profileRepository).findByUserId(1L);
		assertEquals("short-uuid", user.getIdentifier());
	}

	@Test
	@DisplayName("탈퇴 후 5일이 지나지 않은 회원은 익명화되지 않는다")
	void doNotAnonymizeRecentlyDeletedUsers() {
		// given
		LocalDateTime now = LocalDateTime.of(2024, 1, 21, 0, 0);
		LocalDateTime cutoffDate = now.minusDays(5);

		when(userRepository.findByIsDeletedTrueAndUpdatedAtBefore(any(LocalDateTime.class)))
			.thenReturn(Collections.emptyList());

		// when
		try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
			mockedStatic.when(LocalDateTime::now).thenReturn(now);
			userScheduler.anonymizeExpiredUsers();
		}

		// then
		verify(userRepository).findByIsDeletedTrueAndUpdatedAtBefore(cutoffDate);
		verify(profileRepository, never()).findByUserId(any(Long.class));
		verify(uuidShortener, never()).shortenUuid();
	}

	private User createUser(Long id, String identifier, boolean isDeleted, LocalDateTime updatedAt) {
		User user = User.builder()
			.socialType(SocialType.KAKAO)
			.externalId(identifier)
			.build();

		ReflectionTestUtils.setField(user, "id", id);
		ReflectionTestUtils.setField(user, "isDeleted", isDeleted);
		ReflectionTestUtils.setField(user, "updatedAt", updatedAt);

		return user;
	}

	private Profile createProfile(Long id, Long userId, String nickname) {
		Profile profile = Profile.builder()
			.userId(userId)
			.nickname(nickname)
			.gender(GenderType.MALE)
			.ageRange(AgeRangeType.TWENTIES)
			.build();

		ReflectionTestUtils.setField(profile, "id", id);

		return profile;
	}
}