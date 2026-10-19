-- v1.4.8 아티클 수정 이력 — Expand (신규 테이블 1개, 기존 무영향)
-- 게시된 기사 수정(PATCH /admin/article/{id}) 때 바뀌기 전 편집 가능한 필드를 통째로 남긴다. 되돌리기가 이 행으로 복원한다.
-- 적용: 백엔드 배포 전 prod 선적용 (명시 승인 필요).

CREATE TABLE IF NOT EXISTS toronchul_article_revision (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    article_id      BIGINT       NOT NULL,
    headline        VARCHAR(255) NOT NULL,
    summary         TEXT         NULL,
    body            LONGTEXT     NOT NULL,     -- JSON [{text, sources}]
    keywords        TEXT         NULL,         -- JSON [string]
    article_section VARCHAR(32)  NULL,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_toronchul_article_revision_article (article_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 되돌리기: DROP TABLE toronchul_article_revision;
