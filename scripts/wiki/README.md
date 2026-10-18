# 토론위키 생성 — 데이터 레포로 이관됨 (2026-10-19)

위키 초안 생성·근거 대조·DRAFT 업로드·주간 자동화는 **toronchul_data_v1** 이 맡는다.
정본 문서: 데이터 레포 `docs/wiki.md`.

백엔드가 맡는 것:
- API·스키마·서빙 (`POST /api/v1/admin/wiki/drafts`, `GET /api/v2/wiki`)
- 중립성 프롬프트 정본 `WikiPrompts.SYSTEM_PROMPT` — 바꾸면 데이터 쪽에 알린다 (데이터 쪽이 사본을 쓴다)
- 게시 대행 — 사용자가 승인한 revisionId 만, 운영 서버 안의 `wiki-editor` 키로

최초 17건을 만든 로컬 스크립트는 git 이력(ab9ef5e, PR #219)에 남아 있다.
