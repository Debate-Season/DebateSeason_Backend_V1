-- v1.4.5 토론철 발행 아티클 — Expand (신규 테이블 4개, 기존 무영향)
-- 생성·데스킹은 데이터 레포(toronchul_data_v1, PRD v0.11.0 §6), 적재·서빙은 백엔드.
-- 크롤러가 쓰는 운영 `article`(뉴스 본문)과 다른 테이블이다.
-- 적용: 백엔드 배포 전 prod 선적용 (명시 승인 필요).

CREATE TABLE IF NOT EXISTS toronchul_article (
    id                BIGINT        NOT NULL AUTO_INCREMENT,   -- 웹 URL /article/{base36(id)}
    article_key       VARCHAR(80)   NOT NULL,                  -- 데이터 쪽 멱등 키
    edition           VARCHAR(16)   NOT NULL,                  -- toronchul / tmoney
    status            VARCHAR(16)   NOT NULL,                  -- REVIEW / APPROVED / PUBLISHED / HIDDEN / REJECTED / EXCLUDED
    headline          VARCHAR(255)  NOT NULL,
    summary           TEXT          NULL,                      -- 평문 2~3문장
    body              LONGTEXT      NOT NULL,                  -- JSON [{text, sources:[media_id]}]
    article_section   VARCHAR(32)   NULL,
    keywords          TEXT          NULL,                      -- JSON [string]
    debate_point      VARCHAR(255)  NULL,
    byline            VARCHAR(32)   NOT NULL DEFAULT '토론철 AI 기자',
    ai_generated      BOOLEAN       NOT NULL DEFAULT TRUE,
    view_count        INT           NOT NULL DEFAULT 0,
    image_url         VARCHAR(512)  NULL,
    sensitivity_max   DECIMAL(4,3)  NULL,
    sensitivity_axes  TEXT          NULL,                      -- JSON
    generator_version VARCHAR(80)   NULL,
    created_at        DATETIME(6)   NOT NULL,                  -- 데이터 쪽 생성 시각
    updated_at        DATETIME(6)   NULL,
    reviewed_at       DATETIME(6)   NULL,
    reviewed_by       VARCHAR(64)   NULL,
    date_published    DATETIME(6)   NULL,                      -- 처음 PUBLISHED 된 시각 (공개 작성일시)
    PRIMARY KEY (id),
    UNIQUE KEY uk_toronchul_article_key (article_key),
    KEY idx_toronchul_article_public (edition, status, date_published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 출처 (media.id). 공개 응답에는 아직 내지 않는다 (D7 법률 검토)
CREATE TABLE IF NOT EXISTS toronchul_article_source (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    media_id   BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_toronchul_article_source (article_id, media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 아티클 ↔ 사건 (정본 연결). 이슈는 issue_news_event 를 거쳐 얻는다
CREATE TABLE IF NOT EXISTS toronchul_article_event (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    article_id    BIGINT NOT NULL,
    news_event_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_toronchul_article_event (article_id, news_event_id),
    KEY idx_toronchul_article_event_event (news_event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 아티클 ↔ 이슈 직접 연결 (보조. 사건이 없을 때도 이슈 페이지에 붙이기 위함)
CREATE TABLE IF NOT EXISTS toronchul_article_issue (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    issue_id   BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_toronchul_article_issue (article_id, issue_id),
    KEY idx_toronchul_article_issue_issue (issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 되돌리기 (적재 전만):
-- DROP TABLE toronchul_article_issue; DROP TABLE toronchul_article_event; DROP TABLE toronchul_article_source; DROP TABLE toronchul_article;
