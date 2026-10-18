#!/usr/bin/env python3
"""out/sources/{id}.json 으로 로컬 Claude Code(claude -p)를 불러 위키 초안을 만든다 → out/drafts/{id}.json, .md

- 시스템 프롬프트는 백엔드 WikiPrompts.SYSTEM_PROMPT (중립성 규칙·문서 구조) + 아래 RULES
  WikiPrompts 를 고치면 여기 SYSTEM_PROMPT 도 같이 고친다.
- 도구를 모두 끄고(--tools "") 주어진 재료만 보게 한다. 출력은 --json-schema 로 {content, summary} 강제.
- API 키가 필요 없다 — 로컬 Claude Code 로그인으로 돈다. 나중에 cron 으로 자동화할 수 있다.
"""
import argparse
import json
import subprocess
import sys
from datetime import datetime
from pathlib import Path

HERE = Path(__file__).resolve().parent
SOURCES = HERE / "out" / "sources"
DRAFTS = HERE / "out" / "drafts"
DEFAULT_MODEL = "claude-opus-5-5"

# 백엔드 domain/wiki/service/generator/WikiPrompts.SYSTEM_PROMPT 와 같은 내용
SYSTEM_PROMPT = """너는 시사·정치 이슈를 중립적으로 정리하는 백과사전 편집자다.
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
"""

RULES = """
[근거 규칙 — 로컬 생성 추가분]
- 사실의 근거는 아래 "뉴스" 와 "기사 본문" 이다. 날짜·수치·인명·기관명은 재료에 있는 것만 쓴다.
- 재료에 없는 최근 사건을 네 지식으로 채우지 않는다. 네 지식은 오래된 배경 설명(제도·용어·역사)에만 쓴다.
- 뉴스 제목은 보도된 사실이지 확정된 사실이 아닐 수 있다. 엇갈리는 보도는 "~라는 보도가 있다" 로 쓴다.
- `주요 타임라인` 은 재료의 날짜가 있는 사건만 날짜순으로 쓴다. 재료가 없으면 "자료 부족" 이라고 쓴다.
- `참고` 에는 근거로 쓴 기사의 언론사·날짜·제목만 나열한다(URL 금지). 5~10건.

[저작권]
- 기사 문장을 그대로 옮기지 않는다. 사실 단위로 재구성해 네 문장으로 쓴다. 따옴표 인용은 발언에만, 짧게.

[요약 summary]
- 본문과 별도로 2~3문장 평문 요약을 쓴다. 150자 안팎, Markdown·따옴표·이모지 금지. 중립성 규칙 동일.
"""

SCHEMA = {
    "type": "object",
    "properties": {
        "content": {"type": "string", "description": "위키 본문 Markdown"},
        "summary": {"type": "string", "description": "2~3문장 평문 요약, 150자 안팎"},
    },
    "required": ["content", "summary"],
    "additionalProperties": False,
}


def build_prompt(src: dict) -> str:
    issue = src["issue"]
    lines = ["다음 이슈에 대한 토론위키 문서를 작성해줘.", "",
             f"이슈 제목: {issue['title']}",
             f"분류: {issue.get('majorCategory') or '-'} > {issue.get('middleCategory') or '-'}",
             f"이슈 등록일: {issue.get('createdAt')}", ""]
    if src["threads"]:
        lines.append("주요 쟁점(토론 주제):")
        for t in src["threads"]:
            tags = f"  {t['hashtags']}" if t.get("hashtags") else ""
            lines.append(f"- {t['title']}{tags}")
    else:
        lines.append("등록된 쟁점(토론 주제)이 없다. 개요와 배경 중심으로 작성해줘.")

    lines += ["", f"뉴스 (제목 검색 {src['headlineMatches']}건 중 날짜순으로 고르게 {len(src['headlines'])}건):"]
    if not src["headlines"]:
        lines.append("- 없음. 일반적인 배경 설명만 가능하다. 사실 서술은 최소화하고 자료 부족을 밝혀라.")
    for h in src["headlines"]:
        lines.append(f"- {h['date']} [{h.get('outlet') or '-'}] {h['title']}")

    if src["articles"]:
        lines += ["", "기사 본문 (최신, 앞부분만):"]
        for a in src["articles"]:
            lines += [f"### {a['date']} [{a.get('outlet') or '-'}] {a['title']}", a["body"], ""]
    return "\n".join(lines)


def generate(issue_id: str, model: str) -> dict:
    src = json.loads((SOURCES / f"{issue_id}.json").read_text())
    cmd = ["claude", "-p", "--model", model, "--tools", "", "--output-format", "json",
           "--no-session-persistence", "--system-prompt", SYSTEM_PROMPT + RULES,
           "--json-schema", json.dumps(SCHEMA)]
    result = subprocess.run(cmd, input=build_prompt(src), capture_output=True, text=True, timeout=900)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout[:500])
    envelope = json.loads(result.stdout)
    if envelope.get("is_error"):
        raise RuntimeError(str(envelope.get("result"))[:500])
    out = envelope.get("structured_output") or json.loads(envelope["result"])
    return {
        "issueId": int(issue_id),
        "title": src["issue"]["title"],
        "model": model,
        "content": out["content"].strip(),
        "summary": out["summary"].strip(),
        "sourceStats": {"headlineMatches": src["headlineMatches"], "headlinesUsed": len(src["headlines"]),
                        "articles": len(src["articles"])},
        "generatedAt": datetime.now().isoformat(timespec="seconds"),
        "costUsd": envelope.get("total_cost_usd"),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("issue_ids", nargs="*", help="비우면 out/sources 전부")
    parser.add_argument("--model", default=DEFAULT_MODEL)
    parser.add_argument("--force", action="store_true", help="이미 있는 초안도 다시 만든다")
    args = parser.parse_args()

    DRAFTS.mkdir(parents=True, exist_ok=True)
    ids = args.issue_ids or sorted((p.stem for p in SOURCES.glob("*.json")), key=int)
    failed = []
    for issue_id in ids:
        target = DRAFTS / f"{issue_id}.json"
        if target.exists() and not args.force:
            print(f"[{issue_id}] 이미 있음 — 건너뜀 (--force 로 다시)")
            continue
        try:
            draft = generate(issue_id, args.model)
        except Exception as e:  # noqa: BLE001 — 한 건 실패가 나머지를 막지 않게
            print(f"[{issue_id}] 실패: {e}", file=sys.stderr)
            failed.append(issue_id)
            continue
        target.write_text(json.dumps(draft, ensure_ascii=False, indent=1))
        (DRAFTS / f"{issue_id}.md").write_text(
            f"<!-- {draft['title']} | {draft['model']} | {draft['generatedAt']} -->\n\n"
            f"> 요약: {draft['summary']}\n\n{draft['content']}\n")
        print(f"[{issue_id}] {draft['title']}: 본문 {len(draft['content'])}자, 요약 {len(draft['summary'])}자")
    if failed:
        sys.exit(f"실패: {failed}")


if __name__ == "__main__":
    main()
