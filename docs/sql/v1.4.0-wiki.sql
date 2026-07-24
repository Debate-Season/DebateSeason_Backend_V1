-- v1.4.0 AI 토론위키 — Expand (신규 테이블 2개, 기존 무영향)
-- 이슈당 1장(메타) + 본문 이력(리비전). 게시 상태의 단일 진실 = debate_wiki.status + published_revision_id.
-- 적용: prod 선적용 승인 후 (스키마 쓰기는 solo-dev 예외, 명시 승인 필요).

CREATE TABLE IF NOT EXISTS debate_wiki (
    wiki_id               BIGINT       NOT NULL AUTO_INCREMENT,
    issue_id              BIGINT       NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',   -- DRAFT / PUBLISHED / HIDDEN
    published_revision_id BIGINT       NULL,                       -- 현재 게시 중인 리비전 (NULL=미게시)
    created_at            DATETIME(6)  NULL,
    updated_at            DATETIME(6)  NULL,
    PRIMARY KEY (wiki_id),
    UNIQUE KEY uk_debate_wiki_issue (issue_id)                     -- 이슈 1:1 강제
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS debate_wiki_revision (
    revision_id  BIGINT       NOT NULL AUTO_INCREMENT,
    wiki_id      BIGINT       NOT NULL,
    content      MEDIUMTEXT   NOT NULL,                            -- 본문(Markdown)
    source       VARCHAR(20)  NOT NULL,                           -- AI / ADMIN
    model        VARCHAR(64)  NULL,                               -- 생성 모델명(AI), ADMIN 편집은 NULL
    edit_summary VARCHAR(255) NULL,
    created_by   BIGINT       NULL,                               -- ADMIN 편집 시 user_id, AI 생성은 NULL
    created_at   DATETIME(6)  NULL,
    PRIMARY KEY (revision_id),
    KEY idx_wiki_revision_wiki (wiki_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
