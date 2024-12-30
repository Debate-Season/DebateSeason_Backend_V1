-- Create users table first as it's referenced by other tables
CREATE TABLE users
(
    user_id     BIGINT       NOT NULL AUTO_INCREMENT,
    social_type VARCHAR(20)  NOT NULL,
    identifier  VARCHAR(255) NOT NULL,
    community   VARCHAR(100),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE = InnoDB;

-- Create profile table
CREATE TABLE profile
(
    profile_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    nickname   VARCHAR(255) UNIQUE,
    gender     VARCHAR(50),
    age_range  VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (profile_id),
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB;

-- Create community table
CREATE TABLE community
(
    community_id BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    icon_url     VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (community_id)
) ENGINE = InnoDB;

-- Create profile_community table
CREATE TABLE profile_community
(
    id           BIGINT NOT NULL AUTO_INCREMENT,
    profile_id   BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_profile_community_profile FOREIGN KEY (profile_id) REFERENCES profile (profile_id),
    CONSTRAINT fk_profile_community_community FOREIGN KEY (community_id) REFERENCES community (community_id)
) ENGINE = InnoDB;

-- Create issue table
CREATE TABLE issue
(
    issue_id   BIGINT NOT NULL AUTO_INCREMENT,
    title      VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (issue_id)
) ENGINE = InnoDB;

-- Create chat_room table
CREATE TABLE chat_room
(
    chat_room_id BIGINT NOT NULL AUTO_INCREMENT,
    issue_id     BIGINT NOT NULL,
    title        VARCHAR(255),
    content      TEXT,
    yes          INTEGER   DEFAULT 0,
    no           INTEGER   DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chat_room_id),
    CONSTRAINT fk_chatroom_issue FOREIGN KEY (issue_id) REFERENCES issue (issue_id)
) ENGINE = InnoDB;

-- Create chat table
CREATE TABLE chat
(
    chat_id      BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id BIGINT NOT NULL,
    sender       VARCHAR(100),
    category     VARCHAR(50),
    content      TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chat_id),
    CONSTRAINT fk_chat_chatroom FOREIGN KEY (chat_room_id) REFERENCES chat_room (chat_room_id)
) ENGINE = InnoDB;

-- Create user_issue table
CREATE TABLE user_issue
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_userissue_issue FOREIGN KEY (issue_id) REFERENCES issue (issue_id),
    CONSTRAINT fk_userissue_users FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB;

-- Create user_chat_room table
CREATE TABLE user_chat_room
(
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    chat_room_id BIGINT NOT NULL,
    opinion      VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_userchatroom_users FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_userchatroom_chatroom FOREIGN KEY (chat_room_id) REFERENCES chat_room (chat_room_id)
) ENGINE = InnoDB;

-- Create refresh_tokens table
CREATE TABLE refresh_tokens
(
    refresh_token_id BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    token            VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (refresh_token_id),
    CONSTRAINT fk_refreshtoken_users FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB;

-- Insert initial community data
INSERT INTO community (name, icon_url)
VALUES ('디시인사이드', 'community/icons/dcinside.png'),
       ('에펨코리아', 'community/icons/fmkorea.png'),
       ('더쿠', 'community/icons/theqoo.png'),
       ('뽐뿌', 'community/icons/ppomppu.png'),
       ('루리웹', 'community/icons/ruliweb.png'),
       ('엠팍', 'community/icons/mlbpark.png'),
       ('인벤', 'community/icons/inven.png'),
       ('네이트 판', 'community/icons/natepann.png'),
       ('아카라이브', 'community/icons/arca.png'),
       ('클리앙', 'community/icons/clien.png'),
       ('일간베스트', 'community/icons/ilbe.png'),
       ('인스티즈', 'community/icons/instiz.png'),
       ('보배드림', 'community/icons/bobaedream.png'),
       ('웃긴대학', 'community/icons/humoruniv.png'),
       ('오르비', 'community/icons/orbi.png'),
       ('오늘의유머', 'community/icons/todayhumor.png'),
       ('여성시대', 'community/icons/womad.png'),
       ('에브리타임', 'community/icons/everytime.png'),
       ('블라인드', 'community/icons/blind.png'),
       ('Reddit', 'community/icons/reddit.png'),
       ('X', 'community/icons/twitter.png'),
       ('Threads', 'community/icons/threads.png'),
       ('무소속', 'community/icons/independent.png');