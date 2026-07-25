package com.debateseason_backend_v1.domain.wiki.service.generator;

import java.util.List;

/**
 * 토론위키 생성 프롬프트 · 가드레일 (v1.4.0 Phase 2).
 *
 * <p>실제 LLM 호출 구현체가 이 상수/메서드를 그대로 써서 벤더 코드와 프롬프트를 분리한다.
 * 시사·정치 문서라 <b>중립·균형</b>이 최우선 — 시스템 프롬프트에 강제한다.
 */
public final class WikiPrompts {

	private WikiPrompts() {
	}

	/** 기본 생성 모델. 실 구현체가 다른 모델을 쓰면 {@link GeneratedWiki#model()} 에 실제값을 넣는다. */
	public static final String DEFAULT_MODEL = "claude-opus-4-8";

	/**
	 * 중립·균형 가드레일 시스템 프롬프트. 정치·시사 이슈를 한쪽으로 기울지 않게 서술하도록 강제한다.
	 */
	public static final String SYSTEM_PROMPT = """
		너는 시사·정치 이슈를 중립적으로 정리하는 백과사전 편집자다.
		주어진 이슈와 쟁점 목록을 바탕으로 나무위키식 문서를 한국어 Markdown 으로 작성한다.

		[중립성 규칙 — 반드시 지킬 것]
		- 찬성/반대 논거를 대칭적으로, 같은 비중과 온도로 서술한다. 어느 한쪽을 옹호·폄하하지 않는다.
		- 검증된 사실 위주로 쓰고, 단정이 어려운 부분은 "~라는 주장이 있다" 식으로 귀속해 표현한다.
		- 특정 정당·인물·집단에 대한 가치 판단이나 선동적 표현을 넣지 않는다.
		- 모르는 사실을 지어내지 않는다. 자료가 부족하면 그 항목을 비워두거나 일반론으로만 서술한다.

		[출력 형식]
		- 순수 Markdown 본문만 출력한다(코드펜스로 감싸지 말 것).
		- 구조: `## 개요` / `## 배경` / `## 주요 타임라인` / `## 쟁점별 찬반` / `## 참고`.
		- `쟁점별 찬반` 은 주어진 쟁점(스레드)마다 소제목을 만들고 찬/반을 나눠 정리한다.
		""";

	/**
	 * 원천 재료를 사용자 프롬프트로 조립한다.
	 */
	public static String buildUserPrompt(WikiGenerationContext c) {

		StringBuilder sb = new StringBuilder();
		sb.append("다음 이슈에 대한 토론위키 문서를 작성해줘.\n\n");
		sb.append("이슈 제목: ").append(nullToDash(c.issueTitle())).append('\n');
		sb.append("분류: ")
			.append(nullToDash(c.majorCategory()))
			.append(" > ")
			.append(nullToDash(c.middleCategory()))
			.append('\n');

		List<String> threads = c.threadTitles();
		if (threads == null || threads.isEmpty()) {
			sb.append("\n등록된 쟁점(토론 스레드)이 없다. 이슈 제목·분류만으로 개요와 배경 중심으로 작성해줘.\n");
		} else {
			sb.append("\n주요 쟁점(토론 스레드 제목):\n");
			for (String title : threads) {
				sb.append("- ").append(title).append('\n');
			}
		}
		return sb.toString();
	}

	private static String nullToDash(String s) {
		return (s == null || s.isBlank()) ? "-" : s;
	}
}
