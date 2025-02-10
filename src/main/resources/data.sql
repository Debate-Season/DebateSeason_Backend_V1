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
       ('동덕여대 남녀공학 반대 시위', '2024-11-07 08:53:30.676363'),
       ('도널드 트럼프 2기 행정부', '2025-01-20 08:56:30.820470'),
       ('양자 컴퓨터 기술 전망', '2025-01-25 08:53:30.676363'),
       ('DeepSeek 쇼크', '2025-02-03 08:53:30.676363')
       ;

INSERT INTO chat_room (issue_id, title, content, created_at)
VALUES (1, '비상계엄 선포는 불법 내란인가?', '#부정선거 #입법독재 #계엄군 국회 진입 #반국가세력 #의료인 복귀 #환율 #탄핵소추', '2024-12-03 08:51:57.381201'),
       (1, '윤 대통령은 탄핵되어야 하는가?', '#조기 대선 #헌법재판소 재판관 임명 #서울서부지방법원 점거 #카톡 검열', '2024-12-03 08:52:27.692735'),
       (2, '동덕여대는 ''여대''로서 존속해야 하는가?', '#공학 전환 #공학 전환 반대 99.9% #재학생 동맹휴학 #정체성 #학교 발전', '2024-11-07 08:53:30.676363'),
       (2, '이번 시위는 폭동으로 규정해야 하는가?', '#폭력 시위 #기물 훼손 #본관 점거 #학생인권 #학습권 #투쟁 #발언권', '2024-11-07 08:53:43.545345'),
       (3, '미국은 관세전쟁을 중단해야 하는가?', '#무역전쟁 #보호무역 #보편관세 #상호관세 #자유무역협정(FTA) #달러 패권', '2025-01-20 08:59:42.109850'),
       (4, '양자 컴퓨터는 3~5년 내 실용화될 수 있는가?', '#큐비트 #양자 오류 #하드웨어 안전성 #윌로우', '2025-01-25 09:01:30.820470'),
       (4, '블록체인의 보안은 양자 컴퓨팅에서 안전한가?', '#블록체인 #가상화폐 #비트코인', '2025-01-25 09:05:38.637825')
       ;

