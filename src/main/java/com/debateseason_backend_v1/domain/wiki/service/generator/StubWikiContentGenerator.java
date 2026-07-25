package com.debateseason_backend_v1.domain.wiki.service.generator;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link WikiContentGenerator} 의 임시 구현 (v1.4.0 Phase 2 골격).
 *
 * <p>아직 LLM 이 배선되지 않아 호출하면 {@link ErrorCode#WIKI_GENERATION_UNAVAILABLE}(503) 을 던진다.
 * 덕분에 <b>키·SDK 없이도 전체 배관(엔드포인트·리비전 저장·게시 전이)이 컴파일·테스트·배포</b>되고,
 * ADMIN 이 {@code POST /generate} 를 눌러도 서버가 죽지 않고 깔끔한 503 을 돌려준다.
 *
 * <p>{@code ANTHROPIC_API_KEY} 확보 후 실제 Claude 구현체를 추가하고 이 클래스를 제거(또는 실 구현체에
 * {@code @Primary})하면 된다. {@link WikiContentGenerator} javadoc 참고.
 */
@Slf4j
@Component
public class StubWikiContentGenerator implements WikiContentGenerator {

	@Override
	public GeneratedWiki generate(WikiGenerationContext context) {
		log.warn("위키 생성기 미구성 — issueId={} 생성 요청을 503 으로 거절 (ANTHROPIC_API_KEY 대기)",
			context.issueId());
		throw new CustomException(ErrorCode.WIKI_GENERATION_UNAVAILABLE);
	}
}
