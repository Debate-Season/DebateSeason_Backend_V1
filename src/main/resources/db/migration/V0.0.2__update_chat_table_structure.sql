ALTER TABLE chat
    -- category를 userCommunity로 변경
    CHANGE COLUMN category user_community VARCHAR(50),

    -- 새로운 컬럼 추가
    ADD COLUMN message_type VARCHAR(20) AFTER chat_room_id,
    ADD COLUMN opinion_type VARCHAR(20) AFTER sender,
    ADD COLUMN time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 기존 데이터의 time_stamp를 created_at 값으로 업데이트
UPDATE chat
SET time_stamp = created_at;

-- created_at 컬럼 삭제 (time_stamp로 대체)
ALTER TABLE chat
DROP COLUMN created_at;