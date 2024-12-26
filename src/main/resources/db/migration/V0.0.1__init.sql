-- 테이블
CREATE TABLE chat (
    chat_id BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    sender VARCHAR(100),
    category VARCHAR(50),
    content TEXT,
    PRIMARY KEY (chat_id)
) ENGINE=InnoDB;

CREATE TABLE chat_room (
    chat_room_id BIGINT NOT NULL AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    title VARCHAR(255),
    content TEXT,
    yes INTEGER,
    no INTEGER,
    created_at TIMESTAMP,
    PRIMARY KEY (chat_room_id)
) ENGINE=InnoDB;

CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    community VARCHAR(100),
    PRIMARY KEY (user_id)
) ENGINE=InnoDB;

CREATE TABLE issue (
    issue_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) ,
    PRIMARY KEY (issue_id)
) ENGINE=InnoDB;

CREATE TABLE user_issue (
    id BIGINT NOT NULL AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 외래키 제약 조건 추가
ALTER TABLE chat
    ADD CONSTRAINT fk_chat_chatroom
        FOREIGN KEY (chat_room_id)
            REFERENCES chat_room (chat_room_id);

ALTER TABLE chat_room
    ADD CONSTRAINT fk_chatroom_issue
        FOREIGN KEY (issue_id)
            REFERENCES issue (issue_id);

ALTER TABLE user_issue
    ADD CONSTRAINT fk_userissue_issue
        FOREIGN KEY (issue_id)
            REFERENCES issue (issue_id);

ALTER TABLE user_issue
    ADD CONSTRAINT fk_userissue_users
        FOREIGN KEY (user_id)
            REFERENCES users (user_id);