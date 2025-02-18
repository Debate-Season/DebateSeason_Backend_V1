package com.debateseason_backend_v1.domain.user.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.component.UuidShortener;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.User;

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

		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(5);

		List<User> expiredUsers = userRepository.findByIsDeletedTrueAndUpdatedAtBefore(cutoffDate);

		for (User user : expiredUsers) {
			String uuid = uuidShortener.shortenUuid();
			log.info("회원 ID: {} 익명화 처리 중", user.getId());

			user.anonymize(uuid);

			Profile profile = profileRepository.findByUserId(user.getId())
				.orElse(null);

			if (profile != null) {
				profile.anonymize("탈퇴회원#" + uuid);
				log.info("회원 ID: {}의 프로필 익명화 완료", user.getId());
			}
		}

		log.info("총 {}명의 탈퇴 회원 익명화 처리 완료", expiredUsers.size());
	}

}