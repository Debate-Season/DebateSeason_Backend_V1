-- v1.4.2 온톨로지 승인 반영·롤백 — Expand (신규 테이블 4개, 기존 무영향)
-- 선행: v1.4.1-issue-dormant.sql (issue_status_history)
-- 적용: 백엔드 배포 전 prod 선적용 (스키마 쓰기는 명시 승인 필요).
--       배포가 먼저 나가도 /api/v1/admin/** 반영 API 를 부르기 전까지는 이 테이블을 쓰지 않는다.
--       단, 이 배포부터 ARCHIVED 이슈·HIDDEN/DELETED 토론방이 목록에서 빠진다 (적용 전 해당 행 수 확인).

-- 관리 API 변경 이력. (proposal_id, action) 유일 = 같은 제안의 재시도가 두 번 만들지 않는 근거.
CREATE TABLE IF NOT EXISTS admin_change_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    action       VARCHAR(32)  NOT NULL,                -- ISSUE_CREATE / ISSUE_RENAME / TOPIC_CREATE / TOPIC_STATUS / EVENT_LINK / EVENT_UNLINK
    entity_type  VARCHAR(32)  NOT NULL,                -- ISSUE / CHAT_ROOM
    entity_id    BIGINT       NOT NULL,
    before_value TEXT         NULL,                    -- JSON
    after_value  TEXT         NULL,                    -- JSON
    reason       VARCHAR(255) NULL,
    proposal_id  VARCHAR(64)  NULL,                    -- 파이프라인 제안 ID (생성 계열 필수)
    decided_by   VARCHAR(64)  NOT NULL,                -- service:ontology / user:{userId}
    approver     VARCHAR(64)  NULL,                    -- Discord 사용자 ID
    decided_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_change_log_proposal (proposal_id, action),
    KEY idx_admin_change_log_entity (entity_type, entity_id, decided_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 사건. 이름을 event 로 하지 않은 건 MariaDB EVENT 키워드·앱 이벤트와 헷갈리지 않게 하려는 것.
CREATE TABLE IF NOT EXISTS news_event (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    occurred_on     DATE         NULL,
    major_category  VARCHAR(64)  NULL,
    middle_category VARCHAR(64)  NULL,
    created_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 이슈 ⊃ 사건 (M:N). 롤백은 이 행만 지운다.
CREATE TABLE IF NOT EXISTS issue_news_event (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    issue_id    BIGINT      NOT NULL,
    event_id    BIGINT      NOT NULL,
    approved_by VARCHAR(64) NULL,
    created_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_issue_news_event (issue_id, event_id),
    KEY idx_issue_news_event_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 사건의 근거 기사 (media.id)
CREATE TABLE IF NOT EXISTS news_event_media (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_news_event_media (event_id, media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 되돌리기 (이력이 비어 있을 때만):
-- DROP TABLE news_event_media; DROP TABLE issue_news_event; DROP TABLE news_event; DROP TABLE admin_change_log;
