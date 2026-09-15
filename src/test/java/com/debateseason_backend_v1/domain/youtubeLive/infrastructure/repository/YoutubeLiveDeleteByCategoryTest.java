package com.debateseason_backend_v1.domain.youtubeLive.infrastructure.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.debateseason_backend_v1.domain.youtubeLive.application.repository.YoutubeLiveRepository;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

/**
 * 진행 중인 라이브가 없을 때 끝난 방송을 지우는 동작을 고정한다.
 *
 * 크롤러는 eventType=live 로 조회하므로 방송이 없으면 items 가 비고
 * items.get(0) 이 IndexOutOfBoundsException 을 낸다. 예전에는 그 자리에서
 * 로그만 찍고 끝나서, 마지막에 성공했던(=이미 끝난) 방송이 "실시간 Live" 로
 * 계속 나갔다. 2026-09-15 유튜브 파싱 장애 때 18시간 동안 그 상태였다.
 *
 * 이제 그 경로에서 해당 category 행을 지운다. 여기서 검증하는 것은
 * 크롤러가 의존하는 계약 세 가지다.
 *   1) 지우면 fetch 가 null 을 준다 → 다시 켜질 때 save() 분기를 타고 되살아난다
 *   2) 지울 게 없어도 터지지 않는다 → 방송 없는 상태가 이어져도 매 회차 안전하다
 *   3) 다른 category 는 건드리지 않는다 → 한 채널이 쉰다고 나머지가 사라지지 않는다
 */
@ActiveProfiles("test")
@SpringBootTest
class YoutubeLiveDeleteByCategoryTest {

	@Autowired
	private YoutubeLiveRepository youtubeLiveRepository;

	@Autowired
	private YoutubeLiveJpaRepository youtubeLiveJpaRepository;

	@BeforeEach
	void clean() {
		youtubeLiveJpaRepository.deleteAll();
	}

	private void saveLive(String category, String videoId) {
		youtubeLiveRepository.save(YoutubeLiveDto.builder()
			.title(category + " 뉴스 라이브")
			.supplier(category.toUpperCase())
			.videoId(videoId)
			.category(category)
			.createAt(LocalDateTime.of(2026, 9, 15, 23, 0))
			.src("https://i.ytimg.com/vi/" + videoId + "/default.jpg")
			.build());
	}

	@Test
	@DisplayName("지우면 fetch 가 null 을 주고, 다시 저장하면 되살아난다")
	void 지운_뒤_다시_저장하면_되살아난다() {
		saveLive("kbs", "beforeVideo");
		assertThat(youtubeLiveRepository.fetch("kbs")).isNotNull();

		youtubeLiveRepository.deleteByCategory("kbs");

		// null 이어야 크롤러가 update 가 아니라 save 분기를 탄다.
		assertThat(youtubeLiveRepository.fetch("kbs")).isNull();

		// 방송이 다시 켜진 상황.
		saveLive("kbs", "afterVideo");

		assertThat(youtubeLiveRepository.fetch("kbs"))
			.extracting("videoId")
			.isEqualTo("afterVideo");
	}

	@Test
	@DisplayName("지울 행이 없어도 예외를 던지지 않는다")
	void 없는_category_를_지워도_조용하다() {
		// 방송 없는 상태가 이어지면 72분마다 같은 호출이 반복된다.
		// 두 번째부터는 지울 게 없는데, 여기서 터지면 스케줄러가 깨진다.
		assertThatCode(() -> {
			youtubeLiveRepository.deleteByCategory("kbs");
			youtubeLiveRepository.deleteByCategory("kbs");
		}).doesNotThrowAnyException();

		assertThat(youtubeLiveRepository.fetch("kbs")).isNull();
	}

	@Test
	@DisplayName("다른 category 는 건드리지 않는다")
	void 지정한_category_만_지운다() {
		saveLive("kbs", "kbsVideo");
		saveLive("mbc", "mbcVideo");
		saveLive("ytn", "ytnVideo");

		youtubeLiveRepository.deleteByCategory("mbc");

		assertThat(youtubeLiveRepository.fetch("mbc")).isNull();
		assertThat(youtubeLiveRepository.fetch("kbs")).isNotNull();
		assertThat(youtubeLiveRepository.fetch("ytn")).isNotNull();
		assertThat(youtubeLiveRepository.findAll()).hasSize(2);
	}
}
