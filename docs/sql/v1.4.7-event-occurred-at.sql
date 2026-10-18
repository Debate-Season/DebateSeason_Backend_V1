-- v1.4.7 사건 최초 발생 시각 — Expand (컬럼 1개, 기존 무영향)
-- 데이터 쪽이 계산한다: 사건 기사의 가장 이른 보도 시각 (앞선 14일 안의 같은 사건 보도 포함). KST.
-- occurred_on 은 그대로 두고, occurred_at 이 있으면 그 날짜로 맞춘다 (백엔드가 맞춘다).
-- 적용: 백엔드 배포 전 prod 선적용 (명시 승인 필요). 기존 9건은 비어 있다 — 데이터 쪽이 PATCH /admin/event/{id} 로 채운다.

ALTER TABLE news_event
    ADD COLUMN occurred_at DATETIME(6) NULL AFTER occurred_on;

-- 되돌리기: ALTER TABLE news_event DROP COLUMN occurred_at;
