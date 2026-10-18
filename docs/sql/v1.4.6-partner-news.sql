-- v1.4.6 고객사(티머니GO) 뉴스 API — Expand (신규 테이블 3개 + 키워드 색인 채우기, 기존 무영향)
-- 적용: 백엔드 배포 전 prod 선적용 (명시 승인 필요).

-- 아티클 연관 키워드 색인 (정확 일치 검색). 적재 API 가 keywords 와 함께 채운다.
CREATE TABLE IF NOT EXISTS toronchul_article_keyword (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    article_id BIGINT      NOT NULL,
    keyword    VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_toronchul_article_keyword (article_id, keyword),
    KEY idx_toronchul_article_keyword_lookup (keyword, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;  -- 정확 일치: 대소문자·자모 구분

-- 이미 적재된 기사의 keywords(JSON) 를 색인으로 펼친다 (앞뒤 공백 제거·중복 제거)
INSERT IGNORE INTO toronchul_article_keyword (article_id, keyword)
SELECT a.id, TRIM(j.kw)
FROM toronchul_article a,
     JSON_TABLE(a.keywords, '$[*]' COLUMNS (kw VARCHAR(64) PATH '$')) j
WHERE a.keywords IS NOT NULL AND TRIM(j.kw) <> '';

-- 고객사별 조회 수 (D5: 고객사별 분리)
CREATE TABLE IF NOT EXISTS partner_article_view (
    partner    VARCHAR(32) NOT NULL,
    article_id BIGINT      NOT NULL,
    views      BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (partner, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 고객사 호출 미터링 (계약 과금 근거). 일자는 KST.
CREATE TABLE IF NOT EXISTS partner_usage_daily (
    partner    VARCHAR(32) NOT NULL,
    usage_date DATE        NOT NULL,
    endpoint   VARCHAR(32) NOT NULL,              -- news.list / news.detail
    calls      BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (partner, usage_date, endpoint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 되돌리기: DROP TABLE partner_usage_daily; DROP TABLE partner_article_view; DROP TABLE toronchul_article_keyword;
