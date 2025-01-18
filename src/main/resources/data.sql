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

INSERT INTO issue (title, created_at)
VALUES ('윤석열 정부 비상계엄', CURRENT_TIMESTAMP),
       ('동덕여자대학교 남녀공학 전환 반대 시위', CURRENT_TIMESTAMP),
       ('민희진-HYBE 간 ADOR 경영권 분쟁 / NewJeans-ADOR 간 전속계약 해지 분쟁', CURRENT_TIMESTAMP),
       ('2024년 미국 대통령 선거', CURRENT_TIMESTAMP),
       ('제주항공 2216편 활주로 이탈 사고', CURRENT_TIMESTAMP);

INSERT INTO chat_room (issue_id, title, content, created_at)
VALUES (1, '비상계엄 선포의 정당성', '국가 안보와 헌정 질서 수호를 위한 불가피한 조치였다고 주장할 수 있습니다.', CURRENT_TIMESTAMP),
       (1, '비상계엄 선포의 정당성', '이를 정치적 압박에 대응하기 위한 극단적이고 불법적인 조치로 비판할 수 있습니다.', CURRENT_TIMESTAMP),
       (1, '비상계엄 해제 이후의 대통령 책임', '대통령이 국회의 요구를 수용하여 신속히 계엄을 해제했으므로 추가적인 책임을 물을 필요가 없다고 주장할 수 있습니다.',
        CURRENT_TIMESTAMP),
       (1, '비상계엄 해제 이후의 대통령 책임', '위헌적인 계엄 선포에 대해 대통령의 탄핵 등 강력한 책임을 물어야 한다고 주장할 수 있습니다.',
        CURRENT_TIMESTAMP),
       (1, '국회의 정부 견제 방식의 적절성', '이러한 국회의 행위가 행정부와 사법부를 마비시키는 과도한 견제라고 주장할 수 있습니다.',
        CURRENT_TIMESTAMP),
       (1, '국회의 정부 견제 방식의 적절성', '이를 정치적 압박에 대응하기 위한 극단적이고 불법적인 조치로 비판할 수 있습니다.',
        CURRENT_TIMESTAMP),
       (2, '여자대학의 존속 필요성', '여성 교육과 리더십 개발을 위한 특화된 환경이 여전히 필요하다.', CURRENT_TIMESTAMP),
       (2, '여자대학의 존속 필요성', '현대 사회에서 성평등이 진전되어 별도의 여자대학이 불필요하다.', CURRENT_TIMESTAMP),
       (2, '학령인구 감소에 대한 대응 방안', '남녀공학 전환은 학령인구 감소에 대응하는 효과적인 방안이다.', CURRENT_TIMESTAMP),
       (2, '학령인구 감소에 대한 대응 방안', '여자대학으로서의 특성을 유지하면서 다른 방식으로 경쟁력을 확보해야 한다.', CURRENT_TIMESTAMP),
       (2, '대학 운영 결정에서의 학생 참여 범위', '이를 정치적 압박에 대응하기 위한 극단적이고 불법적인 조치로 비판할 수 있습니다.',
        CURRENT_TIMESTAMP),
       (2, '대학 운영 결정에서의 학생 참여 범위', '이를 정치적 압박에 대응하기 위한 극단적이고 불법적인 조치로 비판할 수 있습니다.',
        CURRENT_TIMESTAMP),
       (3, '누가 경영권을 가져야 하는가?', '민희진/HYBE', CURRENT_TIMESTAMP),
       (3, 'HYBE의 감사 착수 정당성', 'HYBE의 감사는 경영권 탈취 시도에 대한 정당한 대응이었다.', CURRENT_TIMESTAMP),
       (3, 'HYBE의 감사 착수 정당성', 'HYBE의 감사는 과도한 조치이며 ADOR의 자율성을 침해했다.', CURRENT_TIMESTAMP),
       (3, '민희진 대표의 기자회견 태도', '민희진 대표의 직설적인 발언은 진실을 밝히기 위한 필요한 조치였다.', CURRENT_TIMESTAMP),
       (3, '민희진 대표의 기자회견 태도', '공식 석상에서의 비속어 사용은 부적절하며 전문성을 해쳤다.', CURRENT_TIMESTAMP),
       (3, 'ADOR의 독립성 보장', 'ADOR의 독립성 보장은 창의성과 경쟁력 유지를 위해 필요하다.', CURRENT_TIMESTAMP),
       (3, 'ADOR의 독립성 보장', 'HYBE의 시스템 하에서 운영되어야 안정적인 경영이 가능하다.', CURRENT_TIMESTAMP),
       (4, '경제 정책 방향', '이를 정치적 압박에 대응하기 위한 극단적이고 불법적인 조치로 비판할 수 있습니다.', CURRENT_TIMESTAMP),
       (4, '경제 정책 방향', '해리스의 중산층 중심 경제 정책이 장기적으로 더 안정적인 성장을 가져올 수 있다.', CURRENT_TIMESTAMP),
       (4, '이민 정책', '국경 강화와 엄격한 이민 정책이 국가 안보와 경제 보호에 필요하다.', CURRENT_TIMESTAMP),
       (4, '이민 정책', '포용적인 이민 정책이 다양성을 증진하고 경제에 긍정적인 영향을 미친다.', CURRENT_TIMESTAMP),
       (4, '기후 변화 대응', '화석 연료 산업 육성이 에너지 독립과 경제 성장에 중요하다.', CURRENT_TIMESTAMP),
       (4, '기후 변화 대응', '친환경 에너지 정책이 장기적인 지속가능성과 국제 경쟁력 확보에 필수적이다.', CURRENT_TIMESTAMP),
       (5, '활주로 착륙 지점의 적절성', '활주로 3분의 1 지점 착륙은 비상 상황에서 불가피한 선택이었으며, 조종사의 판단이 최선이었다.',
        CURRENT_TIMESTAMP),
       (5, '활주로 착륙 지점의 적절성', '활주로 초반부에 착륙했다면 감속 거리를 더 확보할 수 있었고, 사고를 방지할 수 있었을 것이다.',
        CURRENT_TIMESTAMP),
       (5, '조류 충돌 경보 대응의 적절성', '관제탑의 조류 충돌 경보 후 1분 만에 메이데이 선언을 한 것은 신속하고 적절한 대응이었다.',
        CURRENT_TIMESTAMP),
       (5, '조류 충돌 경보 대응의 적절성', '조류 충돌 경보 후 더 빠른 대응과 예방 조치가 필요했으며, 착륙 연기 등 다른 선택지를 고려했어야 한다.',
        CURRENT_TIMESTAMP),
       (5, '동체 착륙 결정의 타당성', '랜딩기어 고장 상황에서 동체 착륙 시도는 불가피한 선택이었으며, 최선의 대안이었다.', CURRENT_TIMESTAMP),
       (5, '동체 착륙 결정의 타당성', '동체 착륙은 위험성이 높은 선택이었으며, 연료 방출 등 추가적인 안전 조치 후 시도했어야 한다.',
        CURRENT_TIMESTAMP);