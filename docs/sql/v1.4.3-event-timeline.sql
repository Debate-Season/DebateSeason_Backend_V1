-- v1.4.3 사건 수정·병합·되돌리기 + 이슈 타임라인 버전
-- 선행: v1.4.2-ontology-admin.sql
-- 적용: 백엔드 배포 전 prod 선적용 (스키마 쓰기는 명시 승인 필요).
--       news_event 는 v1.4.2 에서 만든 빈 테이블이라 ALTER 부담이 없다.
--       chat_room.status 는 VARCHAR(20) 이라 PREPARING 추가에 DDL 이 필요 없다.

-- 병합으로 흡수된 사건 표시. 행은 지우지 않는다.
ALTER TABLE news_event
    ADD COLUMN merged_into BIGINT NULL AFTER middle_category;

-- 이슈 타임라인 버전. 사건 추가·해제·수정·병합·되돌리기마다 1씩 오른다.
CREATE TABLE IF NOT EXISTS issue_timeline_version (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    issue_id      BIGINT      NOT NULL,
    version       INT         NOT NULL,
    change_log_id BIGINT      NOT NULL,                -- admin_change_log.id
    snapshot      TEXT        NOT NULL,                -- JSON [{id, name, date}], 날짜순
    created_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_issue_timeline_version (issue_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 되돌리기:
-- DROP TABLE issue_timeline_version; ALTER TABLE news_event DROP COLUMN merged_into;
