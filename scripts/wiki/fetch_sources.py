#!/usr/bin/env python3
"""운영 DB 에서 이슈별 위키 원천 재료를 읽기 전용으로 모은다 → out/sources/{issueId}.json

- 이슈 메타, 노출 중인 토론 주제(스레드) 제목·해시태그
- 검색어가 제목에 들어간 뉴스: 날짜순으로 고르게 최대 --headlines 건 (타임라인 근거)
- 그중 본문이 있는 최신 기사 최대 --bodies 건 (본문은 --body-chars 자로 자름)

SELECT 만 실행한다. SQL 은 ssh 표준입력으로 넘겨 셸 이스케이프 문제를 피한다.
뉴스 원문은 언론사 저작물이라 out/ 은 git 에 넣지 않는다 (.gitignore).
"""
import argparse
import json
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
OUT = HERE / "out" / "sources"

REMOTE = (
    "cd /home/ubuntu/app && set -a && . ./.env && set +a && "
    'mysql --default-character-set=utf8mb4 -N --batch --raw -h 127.0.0.1 '
    '-u "$DB_USERNAME" -p"$DB_PASSWORD" debate_season'
)


def query(sql: str) -> list[dict]:
    """JSON_OBJECT 한 줄 = 한 행으로 받는다."""
    result = subprocess.run(["ssh", "debateseason-prod", REMOTE], input=sql, capture_output=True, text=True)
    if result.returncode != 0:
        sys.exit(f"쿼리 실패: {result.stderr.strip()}")
    return [json.loads(line) for line in result.stdout.splitlines() if line.strip()]


def lit(value: str) -> str:
    return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"


def evenly(rows: list, n: int) -> list:
    """날짜순 목록에서 처음·끝을 포함해 고르게 n 건 고른다. 타임라인이 한쪽으로 쏠리지 않게."""
    if len(rows) <= n:
        return rows
    step = (len(rows) - 1) / (n - 1)
    return [rows[round(i * step)] for i in range(n)]


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("issue_ids", nargs="*", help="비우면 issues.json 전부")
    parser.add_argument("--headlines", type=int, default=150)
    parser.add_argument("--bodies", type=int, default=8)
    parser.add_argument("--body-chars", type=int, default=1500)
    args = parser.parse_args()

    config = {k: v for k, v in json.loads((HERE / "issues.json").read_text()).items() if not k.startswith("_")}
    ids = args.issue_ids or list(config)
    OUT.mkdir(parents=True, exist_ok=True)

    for issue_id in ids:
        cfg = config[issue_id]
        meta = query(f"""
            SELECT JSON_OBJECT('issueId', issue_id, 'title', title, 'majorCategory', major_category,
                               'middleCategory', middle_category, 'status', status, 'createdAt', DATE(created_at))
            FROM issue WHERE issue_id = {int(issue_id)};""")
        if not meta:
            print(f"[{issue_id}] 이슈 없음 — 건너뜀")
            continue
        threads = query(f"""
            SELECT JSON_OBJECT('title', title, 'hashtags', content, 'status', status)
            FROM chat_room
            WHERE issue_id = {int(issue_id)} AND (room_type IS NULL OR room_type = 'THREAD')
              AND status IN ('OPEN','CLOSED')
            ORDER BY chat_room_id;""")

        match = " OR ".join(f"m.title LIKE {lit('%' + t + '%')}" for t in cfg["terms"])
        period = ""
        if cfg.get("since"):
            period += f" AND m.created_at >= {lit(cfg['since'])}"
        if cfg.get("until"):
            period += f" AND m.created_at < DATE_ADD({lit(cfg['until'])}, INTERVAL 1 DAY)"
        headlines = query(f"""
            SELECT JSON_OBJECT('mediaId', m.id, 'date', DATE(m.created_at), 'outlet', m.media,
                               'title', m.title, 'url', m.url, 'hasBody', a.id IS NOT NULL)
            FROM media m LEFT JOIN article a ON a.media_id = m.id
            WHERE m.type = 'news' AND ({match}){period}
            ORDER BY m.created_at, m.id;""")
        # 같은 제목이 여러 매체·여러 번 크롤링된다. 제목 기준으로 처음 것만 남긴다.
        seen, unique = set(), []
        for h in headlines:
            if h["title"] not in seen:
                seen.add(h["title"])
                unique.append(h)

        body_ids = [h["mediaId"] for h in unique if h["hasBody"]][-args.bodies:]
        bodies = []
        if body_ids:
            bodies = query(f"""
                SELECT JSON_OBJECT('mediaId', m.id, 'date', DATE(m.created_at), 'outlet', m.media, 'title', m.title,
                                   'body', LEFT(a.content, {int(args.body_chars)}))
                FROM media m JOIN article a ON a.media_id = m.id
                WHERE m.id IN ({",".join(str(int(i)) for i in body_ids)})
                ORDER BY m.created_at;""")

        source = {
            "issue": meta[0],
            "threads": threads,
            "search": cfg,
            "headlineMatches": len(unique),
            "headlines": evenly(unique, args.headlines),
            "articles": bodies,
        }
        (OUT / f"{issue_id}.json").write_text(json.dumps(source, ensure_ascii=False, indent=1))
        print(f"[{issue_id}] {meta[0]['title']}: 토론 주제 {len(threads)}, 뉴스 제목 {len(unique)}건 중 "
              f"{len(source['headlines'])}건, 본문 {len(bodies)}건")


if __name__ == "__main__":
    main()
