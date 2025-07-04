package com.debateseason_backend_v1.domain.user.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.component.UuidShortener;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.application.service.ProfileRepository;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.user.application.UserRepository;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserScheduler {

	private final UuidShortener uuidShortener;
	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;

	@Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
	@Transactional
	public void anonymizeExpiredUsers() {
		log.info("탈퇴 회원 익명화 처리 시작: {}", LocalDateTime.now());

		List<User> expiredUsers = userRepository.findByStatus(UserStatus.WITHDRAWAL_PENDING);

		for (User user : expiredUsers) {
			String uuid = uuidShortener.shortenUuid();
			log.info("회원 ID: {} 익명화 처리 중", user.getId());

			if (user.canAnonymizeBySchedule()) {
				user.anonymize(uuid);

				Profile profile = profileRepository.findByUserId(user.getId())
					.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

				profile.anonymize(uuid);
				log.info("회원 ID: {}의 프로필 익명화 완료", user.getId());

				userRepository.save(user);
				profileRepository.save(profile);
			}
		}

		log.info("총 {}명의 탈퇴 회원 익명화 처리 완료", expiredUsers.size());
	}

}