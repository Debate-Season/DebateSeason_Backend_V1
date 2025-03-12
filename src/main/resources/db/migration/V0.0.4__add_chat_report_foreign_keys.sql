-- chat_report_reason 테이블에 chat_report 외래 키 추가
ALTER TABLE chat_report_reason
ADD CONSTRAINT fk_chat_report_reason_report_id
    FOREIGN KEY (report_id)
    REFERENCES chat_report (id)
    ON DELETE CASCADE; 