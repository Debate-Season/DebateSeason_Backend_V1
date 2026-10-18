# 토론위키 로컬 생성

서버 안 LLM 호출 대신 **로컬 Claude Code(`claude -p`)** 로 위키 초안을 만들어 운영에 DRAFT 로 올린다.
API 키가 필요 없고, 나중에 cron 으로 묶으면 자동화된다. (결정 2026-10-19)

```
fetch_sources.py   운영 DB (읽기 전용) → out/sources/{issueId}.json   이슈·토론 주제·뉴스 제목·본문
generate.py        out/sources → claude -p → out/drafts/{issueId}.json, .md
upload.py          out/drafts → POST /api/v1/admin/wiki/drafts   (기본 시험 실행, --apply 로 업로드)
```

```bash
python3 fetch_sources.py            # 전부 (또는 이슈 id 나열)
python3 generate.py                 # 이미 있는 초안은 건너뜀, 다시 만들려면 --force
python3 upload.py                   # 무엇을 올릴지 확인
python3 upload.py --apply           # DRAFT 업로드 (게시 안 함)
```

## 규칙

- **게시는 사람이 한다.** 서비스 키로는 DRAFT 업로드까지만 된다. 게시는 ADMIN 이 검토 후 `POST /api/v1/admin/wiki/publish`.
- 중립성 프롬프트는 백엔드 `WikiPrompts.SYSTEM_PROMPT` 와 같다. 그쪽을 고치면 `generate.py` 도 고친다.
- 근거는 크롤링 뉴스다. 이슈별 검색어·기간은 `issues.json`. 이슈를 새로 만들면 여기에 검색어를 추가한다.
  - 뉴스 제목은 2025-06-02 이후, 본문은 2026-09-23 이후만 있다. 그 전 사건은 근거가 얇다.
  - 데이터 파이프라인 군집(`create_issue`)은 시험 실행분뿐이라 쓰지 않는다.
- `out/` 은 뉴스 원문이 들어 있어 git 에 넣지 않는다.
- `claude -p` 는 도구를 모두 끄고(`--tools ""`) 주어진 재료만 본다. 1건에 1분 안팎.
