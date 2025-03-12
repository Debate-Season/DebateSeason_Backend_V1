-- 신고 사유 테이블 생성
CREATE TABLE IF NOT EXISTS report_reason (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reason_type VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(100)
);

-- 신고 사유 데이터 초기화
INSERT IGNORE INTO report_reason (reason_type, description) VALUES
('ABUSE', '욕설/비방/혐오'),
('ADULT', '음란/청소년 유해'),
('UNLAWFUL', '허위 사실/불법 정보'),
('SPAM', '도배/스팸'),
('ADVERTISING', '홍보/영리 목적'),
('PERSONAL_INFO', '개인정보 노출/유포/거래'),
('OTHER', '기타');

-- 신고-사유 연결 테이블 생성 (중간 엔티티) 채팅과 채팅신고가 다대다여서 추가
CREATE TABLE IF NOT EXISTS chat_report_reason (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    reason_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chat_report_reason UNIQUE (report_id, reason_id)
);

-- 외래 키 제약 조건 추가 (chat_report 테이블이 생성된 후에 실행)
ALTER TABLE chat_report_reason
ADD CONSTRAINT fk_chat_report_reason_reason_id
    FOREIGN KEY (reason_id)
    REFERENCES report_reason (id)
    ON DELETE CASCADE; 