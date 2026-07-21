-- 미적용 (운영 반영 전)
--
-- 배경:
--   v1.3 에서 이슈/채팅방에 수정·삭제 권한을 걸려면 "누가 만들었는가"를 알아야 하는데,
--   지금까지 작성자를 기록하지 않았다.
--   - ChatRoomControllerV1.save() 는 principal 을 파라미터로 받지도 않았다
--   - issue / chat_room 둘 다 created_by, updated_at, status 컬럼이 없다
--   그 결과 수정/삭제 API 를 만들 근거 자체가 없었다(실제로 생성+조회만 존재).
--
--   또한 하드 삭제 대신 소프트 삭제를 하고, 신고 누적 시 노출을 내리는 훅을 붙일
--   자리를 만들기 위해 status 를 함께 도입한다.
--
-- 영향:
--   기존 데이터: issue 17행, chat_room 26행.
--   created_by 는 nullable 로 추가하며 기존 행은 NULL 로 남는다.
--     -> NULL = "시스템/수동 생성" 으로 해석하고, 소유권 판정 시 ADMIN 만 수정 가능하게 다룬다.
--     -> 임의의 관리자 id 로 채우지 않는다. 실제로 누가 만들었는지 알 수 없기 때문이다.
--   status 는 기존 행이 지금 노출 중이므로 각각 PUBLISHED / OPEN 으로 채운다.
--     -> DEFAULT 를 주므로 별도 UPDATE 는 필요 없다.
--
-- 중요 — 이 단계에서는 조회 필터를 걸지 않는다:
--   status 컬럼만 추가하고, 목록/조회 쿼리에 WHERE status = ... 를 넣지 않는다.
--   지금 필터를 걸면 기본값 실수 하나로 프론트에서 목록이 통째로 비어 보일 수 있다.
--   노출 규칙은 위키 상태와 함께 v1.3.6 에서 켠다.
--
-- 호환성:
--   컬럼 추가만 하는 Expand 단계라 구/신 클라이언트 모두 무영향.
--   응답 스펙도 바뀌지 않는다.
--
-- 이 프로젝트는 Flyway/Liquibase 를 쓰지 않고 ddl-auto: none 이므로
-- 스키마 변경은 수동 적용하고 이 디렉터리에 이력을 남긴다.

ALTER TABLE issue
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    ADD COLUMN updated_at DATETIME(6) NULL;

ALTER TABLE chat_room
    ADD COLUMN created_by BIGINT NULL,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    ADD COLUMN updated_at DATETIME(6) NULL;

-- 적용 후 확인용 (전부 PUBLISHED / OPEN, created_by 는 전부 NULL 이어야 한다)
--   SELECT status, COUNT(*), COUNT(created_by) FROM issue     GROUP BY status;
--   SELECT status, COUNT(*), COUNT(created_by) FROM chat_room GROUP BY status;
