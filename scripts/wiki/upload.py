#!/usr/bin/env python3
"""out/drafts/{id}.json 을 운영에 DRAFT 리비전으로 올린다 (POST /api/v1/admin/wiki/drafts).

기본은 시험 실행 — 무엇을 올릴지 보여 주기만 한다. 실제 업로드는 --apply.
게시는 하지 않는다. 게시(/publish)는 사람이 검토한 뒤 ADMIN 이 한다.

서비스 키: 환경변수 TORONCHUL_SERVICE_KEY, 없으면 데이터 레포 .env 의 ONTOLOGY_SERVICE_KEY.
키 값은 출력하지 않는다.
"""
import argparse
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path

HERE = Path(__file__).resolve().parent
DRAFTS = HERE / "out" / "drafts"
DATA_REPO_ENV = HERE.parents[2] / "toronchul_data_v1" / ".env"
DEFAULT_BASE = "https://api.toronchul.app/prod"


def service_key() -> str:
    key = os.environ.get("TORONCHUL_SERVICE_KEY")
    if not key and DATA_REPO_ENV.exists():
        for line in DATA_REPO_ENV.read_text().splitlines():
            if line.startswith("ONTOLOGY_SERVICE_KEY="):
                key = line.split("=", 1)[1].strip().strip("'\"")
    if not key:
        sys.exit("서비스 키가 없습니다. TORONCHUL_SERVICE_KEY 를 설정하세요.")
    return key


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("issue_ids", nargs="*", help="비우면 out/drafts 전부")
    parser.add_argument("--apply", action="store_true", help="실제로 업로드한다")
    parser.add_argument("--base", default=DEFAULT_BASE)
    args = parser.parse_args()

    ids = args.issue_ids or sorted((p.stem for p in DRAFTS.glob("*.json")), key=int)
    key = service_key() if args.apply else None
    for issue_id in ids:
        draft = json.loads((DRAFTS / f"{issue_id}.json").read_text())
        stats = draft["sourceStats"]
        note = (f"로컬 claude -p 생성 {draft['generatedAt'][:10]}, 뉴스 제목 {stats['headlinesUsed']}건"
                f"(검색 {stats['headlineMatches']}건)·본문 {stats['articles']}건 근거")
        body = {"issueId": draft["issueId"], "content": draft["content"], "summary": draft["summary"],
                "model": draft["model"], "editSummary": note}
        if not args.apply:
            print(f"[{issue_id}] {draft['title']}: 본문 {len(draft['content'])}자 — 시험 실행 (--apply 로 업로드)")
            continue

        request = urllib.request.Request(
            f"{args.base}/api/v1/admin/wiki/drafts", data=json.dumps(body).encode(), method="POST",
            headers={"Content-Type": "application/json", "X-Service-Key": key})
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                data = json.loads(response.read())["data"]
            print(f"[{issue_id}] {draft['title']}: revisionId={data['revisionId']} status={data['status']}")
        except urllib.error.HTTPError as e:
            print(f"[{issue_id}] 실패 HTTP {e.code}: {e.read().decode()[:300]}", file=sys.stderr)


if __name__ == "__main__":
    main()
