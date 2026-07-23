-- v1.3.5 Expand — 채팅방 스레드 통합 (하위호환 스키마 추가)
-- 적용 대상: prod (application-prod.yml 은 ddl-auto: none 이므로 수동 적용 필수)
-- 성격: 추가만(additive). 구/신 클라이언트 모두 무영향. 데이터 이관은 별도(v1.3.5-migrate.sql, Phase 2).
--
-- ⚠ 순서: 이 DDL 을 prod 에 먼저 적용한 뒤에야 thread_id 를 쓰는 애플리케이션 코드를 배포할 수 있다.
--        (코드를 먼저 배포하면 thread_id 컬럼이 없는 chat 테이블에 INSERT 가 실패한다.)
-- MariaDB 10.6 / chat 386행 → 모든 ALTER 는 순간.

-- 1) chat_room: 컨테이너/스레드 구분 + 컨테이너 역참조
--    room_type NULL = 레거시 = THREAD 로 해석 (마이그레이션 전 기존 방)
ALTER TABLE chat_room
    ADD COLUMN room_type VARCHAR(16) NULL,
    ADD COLUMN container_room_id BIGINT NULL;

-- 2) chat: 스레드 참조 컬럼
--    thread_id NULL = 미분류 (마이그레이션 전 기존 메시지 / 구 앱 전송분)
ALTER TABLE chat
    ADD COLUMN thread_id BIGINT NULL;

-- 3) 커서/스레드 필터 인덱스
--    chat 은 가장 빨리 커지는 테이블 — 인덱스 없이 스레드 필터를 열면 즉시 느려진다.
ALTER TABLE chat
    ADD INDEX idx_chat_room_cursor (chat_room_id, chat_id),
    ADD INDEX idx_chat_thread_cursor (thread_id, chat_id);

-- 롤백:
-- ALTER TABLE chat DROP INDEX idx_chat_thread_cursor, DROP INDEX idx_chat_room_cursor;
-- ALTER TABLE chat DROP COLUMN thread_id;
-- ALTER TABLE chat_room DROP COLUMN container_room_id, DROP COLUMN room_type;
