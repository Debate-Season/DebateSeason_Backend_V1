INSERT INTO community (name, icon_url)
VALUES ('디시인사이드', 'community/icons/dcinside.png'),
       ('에펨코리아', 'community/icons/fmkorea.png'),
       ('더쿠', 'community/icons/theqoo.png'),
       ('뽐뿌', 'community/icons/ppomppu.png'),
       ('루리웹', 'community/icons/ruliweb.png'),
       ('엠팍', 'community/icons/mlbpark.png'),
       ('인벤', 'community/icons/inven.png'),
       ('네이트판', 'community/icons/natepann.png'),
       ('아카라이브', 'community/icons/arcalive.png'),
       ('클리앙', 'community/icons/clien.png'),
       ('일간베스트', 'community/icons/ilbe.png'),
       ('인스티즈', 'community/icons/instiz.png'),
       ('보배드림', 'community/icons/bobaedream.png'),
       ('웃긴대학', 'community/icons/humoruniv.png'),
       ('오르비', 'community/icons/orbi.png'),
       ('오늘의유머', 'community/icons/todayhumor.png'),
       ('여성시대', 'community/icons/womensgeneration.png'),
       ('에브리타임', 'community/icons/everytime.png'),
       ('블라인드', 'community/icons/blind.png'),
       ('Reddit', 'community/icons/reddit.png'),
       ('X', 'community/icons/x.png'),
       ('Threads', 'community/icons/threads.png'),
       ('무소속', 'community/icons/independent.png');

INSERT INTO issue (title, created_at)
VALUES ('윤석열 정부 비상계엄', '2024-12-03 08:51:57.381201'),
       ('동덕여자대학교 남녀공학 전환 반대 시위', '2024-11-07 08:53:30.676363'),
       ('민희진-HYBE 간 ADOR 경영권 분쟁 / NewJeans-ADOR 간 전속계약 해지 분쟁', '2024-04-25 08:54:56.143396'),
       ('도널드 트럼프 2기 행정부', '2024-01-23 08:56:30.820470')
       

INSERT INTO chat_room (issue_id, title, content, created_at)
VALUES (1, '비상계엄 선포는 정당한가?', 'string', '2024-12-03 08:51:57.381201'),
       (1, '대통령은 비상계엄에 대한 법적 책임을 져야 하는가?', 'string.', '2024-12-03 08:52:27.692735'),
       (1, '국회의 정부 견제 방식은 적절했는가?', 'string', '2024-12-03 08:52:46.590000'),
       (2, '여자대학의 존속 필요성', 'string', '2024-11-07 08:53:30.676363'),
       (2, '학령인구 감소에 대한 대응 방안', 'string', '2024-11-07 08:53:43.545345'),
       (2, '대학 운영 결정에서의 학생 참여 범위', 'string', '2024-01-22 08:54:11.009028'),
       (3, '누가 경영권을 가져야 하는가', 'string', '2024-04-25 08:54:42.109850'),
       (3, 'HYBE의 감사 착수 정당성', 'string', '2024-04-25 08:54:56.143396'),
       (3, '민희진 대표의 기자회견 태도', 'string', '2024-04-25 08:55:34.303370'),
       (3, 'ADOR의 독립성 보장', 'string', '2024-04-25 08:55:46.699187'),
       (4, '경제 정책 방향', 'string', '2024-01-23 08:56:30.820470'),
       (4, '외교', 'string', '2024-01-23 08:56:38.637825'),
       (4, '코인', 'string', '2024-01-23 08:56:46.493262')