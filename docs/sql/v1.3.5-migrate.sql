-- v1.3.5 Migrate — 이슈당 채팅방 통합 (Phase 2)
-- 선행: v1.3.5-expand.sql 적용 완료 + Phase 3a(라우팅) 코드 배포 완료.
-- 성격: 프로덕션 데이터 쓰기. 반드시 백업 후, 트랜잭션 dry-run(ROLLBACK)으로 검증한 뒤 실행.
--
-- 모델: 이슈당 CONTAINER 방 1개를 새로 만들고, 기존 방을 THREAD 로 강등,
--       메시지의 chat_room_id 를 컨테이너로 옮기고 원래 방 id 를 thread_id 에 남긴다.
--       thread_id=NULL 은 "전체/미분류"로 취급하므로 별도 "전체" 스레드 행은 만들지 않는다.
--       찬반·멤버십·알림(user_chat_room, chatroom_notification_setting)은 스레드(옛 방)에 그대로 둔다.

-- Step 1: 이슈별 컨테이너 방 1개 생성 (기존 방이 있는 이슈만)
INSERT INTO chat_room (issue_id, title, status, room_type, created_at, updated_at)
SELECT d.issue_id, i.title, 'OPEN', 'CONTAINER', NOW(6), NOW(6)
FROM (SELECT DISTINCT issue_id FROM chat_room WHERE room_type IS NULL) d
JOIN issue i ON i.issue_id = d.issue_id;

-- Step 2: 기존 방을 THREAD 로 강등하고 소속 컨테이너를 가리키게 한다
UPDATE chat_room t
JOIN chat_room c ON c.issue_id = t.issue_id AND c.room_type = 'CONTAINER'
SET t.room_type = 'THREAD', t.container_room_id = c.chat_room_id
WHERE t.room_type IS NULL;

-- Step 3: 메시지를 컨테이너로 재지정, 원래 방을 thread_id 로 태그
--   RHS 를 조인 테이블 t 로만 참조해 컬럼 대입 순서 의존성을 제거(멀티테이블 UPDATE 안전).
UPDATE chat ch
JOIN chat_room t ON t.chat_room_id = ch.chat_room_id
SET ch.thread_id = t.chat_room_id, ch.chat_room_id = t.container_room_id
WHERE t.room_type = 'THREAD' AND t.container_room_id IS NOT NULL;

-- ── 롤백 (컨테이너 생성 직후 id 범위를 안다는 전제; 실제로는 백업 복원을 권장) ──
-- UPDATE chat ch JOIN chat_room t ON t.chat_room_id = ch.thread_id
--   SET ch.chat_room_id = ch.thread_id, ch.thread_id = NULL
--   WHERE ch.thread_id IS NOT NULL;
-- UPDATE chat_room SET room_type = NULL, container_room_id = NULL WHERE room_type = 'THREAD';
-- DELETE FROM chat_room WHERE room_type = 'CONTAINER';
