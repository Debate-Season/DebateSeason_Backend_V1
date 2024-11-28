-- Create sequences
CREATE SEQUENCE chat_room_seq START WITH 1 INCREMENT BY 50 NOCACHE;
CREATE SEQUENCE chat_seq START WITH 1 INCREMENT BY 50 NOCACHE;
CREATE SEQUENCE client_seq START WITH 1 INCREMENT BY 50 NOCACHE;
CREATE SEQUENCE issue_seq START WITH 1 INCREMENT BY 50 NOCACHE;
CREATE SEQUENCE user_issue_seq START WITH 1 INCREMENT BY 50 NOCACHE;

-- Create tables
CREATE TABLE chat (
    id BIGINT NOT NULL,
    chat_room_id BIGINT,
    sender VARCHAR(100),
    category VARCHAR(50),
    content TEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE chat_room (

    id BIGINT NOT NULL,
    issue_issue_id BIGINT,
    title VARCHAR(255),
    content TEXT, ,
    yes INTEGER NOT NULL,
    no INTEGER NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE client (
    user_id BIGINT NOT NULL,
    username VARCHAR(100),
    password VARCHAR(255),
    role VARCHAR(50),
    community VARCHAR(100),
    PRIMARY KEY (user_id)
) ENGINE=InnoDB;

CREATE TABLE issue (
    issue_id BIGINT NOT NULL,
    title VARCHAR(255),
    PRIMARY KEY (issue_id)
) ENGINE=InnoDB;

CREATE TABLE user_issue (
    id BIGINT NOT NULL,
    issue_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Add foreign key constraints
ALTER TABLE chat
    ADD CONSTRAINT fk_chat_chatroom
        FOREIGN KEY (chat_room_id)
            REFERENCES chat_room (id);

ALTER TABLE chat_room
    ADD CONSTRAINT fk_chatroom_issue
        FOREIGN KEY (issue_issue_id)
            REFERENCES issue (issue_id);

ALTER TABLE user_issue
    ADD CONSTRAINT fk_userissue_issue
        FOREIGN KEY (issue_id)
            REFERENCES issue (issue_id);

ALTER TABLE user_issue
    ADD CONSTRAINT fk_userissue_client
        FOREIGN KEY (user_id)
            REFERENCES client (user_id);