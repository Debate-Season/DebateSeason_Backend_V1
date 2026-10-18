-- v1.4.1 잠자는 이슈 — Expand (신규 테이블 1개, 기존 무영향)
-- issue.status 는 VARCHAR(20) 이라 DORMANT 추가에 DDL 이 필요 없다. 여기서는 전이 이력만 만든다.
-- 적용: 백엔드 배포 전 prod 선적용 (스키마 쓰기는 명시 승인 필요).
--       배포만 먼저 나가도 PATCH /api/v1/admin/issue/{id}/status 호출 전까지는 이 테이블을 쓰지 않는다.

CREATE TABLE IF NOT EXISTS issue_status_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    issue_id    BIGINT       NOT NULL,
    from_status VARCHAR(20)  NOT NULL,                 -- PUBLISHED / DORMANT
    to_status   VARCHAR(20)  NOT NULL,
    reason      VARCHAR(255) NULL,                     -- 예: 14일간 새 사건 없음
    decided_by  VARCHAR(64)  NOT NULL,                 -- service:ontology / user:{userId}
    approver    VARCHAR(64)  NULL,                     -- Discord 사용자 ID (승인자)
    decided_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_issue_status_history_issue (issue_id, decided_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 되돌리기 (Contract 전, 이력이 비어 있을 때만):
-- DROP TABLE issue_status_history;
